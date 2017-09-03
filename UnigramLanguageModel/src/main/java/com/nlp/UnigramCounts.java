package com.nlp;

/**
 *
 * @author jrdodson
 */
public class UnigramCounts {
    public static void main(String...args) {
	if(args.length == 0) {		
		return;
	}
        new UnigramLanguageModel()
                .withCorpus(args[0])
                .fit()
                .getCounts()
                .forEach((token,count) -> 
                        System.out.println(new StringBuilder()
                                .append(token)
                                .append("\t")
                                .append(count)
                                .toString()));
    }
}
