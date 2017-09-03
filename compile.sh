#! /bin/bash
mvn clean -f UnigramLanguageModel/ &&
mvn compile -f UnigramLanguageModel/ && \
mvn package -f UnigramLanguageModel/ 
