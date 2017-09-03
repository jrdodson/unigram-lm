package com.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Unigram language model class using Java 8 streams and Apache Tika.
 * Given a corpus object, ingest and parse to collect word frequencies.
 * Relatively simple and hackable for extra functionality
 * 
 * @author jrdodson
 */
public class UnigramLanguageModel {
    private final Map<String, Long> gramCounts;
    private Set<String> targets;
    private final AutoDetectParser parser;
  
    public UnigramLanguageModel() {
        this.gramCounts = new ConcurrentHashMap<>();
        this.targets = new HashSet<>(); 
        this.parser = new AutoDetectParser(); //instantiating this beforehand remediates alot of overhead later
    }
    
    /**
     * Given a directory path, walk the directory and add each child file as a corpus target.
     * @param corpus, a directory path
     * @return updated UnigramLanguageModel object
     */
    public UnigramLanguageModel withCorpus(String corpus) {
        try{
            Files.newDirectoryStream(Paths.get(corpus))
                    .forEach(path -> this.targets.add(path.toAbsolutePath().toString()));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return this;
    }
    
    /**
     * Fit new data to the language model by updating unigram frequencies
     * @return updated UnigramLanguageModel object
     */
    public UnigramLanguageModel fit() {
        this.targets.stream()
               // .parallel()
                .flatMap(src -> Stream.of(new DocumentIngest(this.parser)
                            .apply(src)  
                            .chars()
                            .map(bit -> (97 <= bit && bit <= 122) || (bit == 39) ? bit : ' ') 
                            .mapToObj(bit -> String.valueOf((char)bit))
                            .collect(Collectors.joining())
                            .split(" ")))                           
                .map(token -> token.replaceAll("^\'+|\'+$", ""))
                .filter(token -> !StringUtils.isEmpty(token))
                .forEach(token -> incrementOrSet(token));        
        this.targets = new HashSet<>(); //reset
        return this;
    }
    
    /**
     * Get the current frequencies
     * @return map of gram->frequency
     */
    public Map<String,Long> getCounts() {
         return this.gramCounts //sort and return
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, 
                                          Map.Entry::getValue,
                                          (v1, v2) -> {throw new IllegalStateException();}, // we don't need to merge anything; otherwise, fail explicitly 
                                          LinkedHashMap::new));
    }
    /**
     * Add or increment the gram frequency
     * @param gram 
     */
    private void incrementOrSet(String gram) {
        long freq = this.gramCounts.containsKey(gram) ? this.gramCounts.get(gram) : 0L;
        this.gramCounts.put(gram, freq+1L);
    }
    
    /**
     * Function object to ingest a directory path and return a stream of raw contents
     * using Apache Tika.
     */
    private static class DocumentIngest implements Function<String, String> {
        private final AutoDetectParser parser;
        
        public DocumentIngest(final AutoDetectParser parser) {
            this.parser = parser;
        }
        
        /**
         * Use Tika to parse an InputStream and get the raw file contents.
         * Lots of boilerplate, but the parser.parse() call populates the 
         * BodyContentHandler object with the file contents.
         * 
         * @param src, a stringified filepath 
         * @return raw string contents
         */
        @Override
        public String apply(String src) {
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            BodyContentHandler handler = new BodyContentHandler(-1);
            try(InputStream stream = Files.newInputStream(Paths.get(src))) {                
                parser.parse(stream, handler, metadata, context);         
            } catch(IOException | SAXException | TikaException ex) {
                ex.printStackTrace();               
            }             
            return handler.toString()
                    .toLowerCase()
                    .replaceAll("<[^>]+>", "");            
        }
    }

}