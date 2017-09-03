### unigram-lm
Simple language model utility for computing unigram frequencies.

#### Requirements

* JAVA_HOME must point to a Java 8 installation
* Maven 3.x must be installed

#### Usage

* To run as a command line utility, execute:
	```
	bash compile.sh
	./unigram-count [corpus_directory] > [output_file]
	```
* To run in code, add the compiled jar as a Maven dependency and use the API as follows:
	```
	Map<String, Long> counts = new UnigramLanguageModel()
                .withCorpus("corpus_directory")
                .fit()
                .getCounts()
	```
