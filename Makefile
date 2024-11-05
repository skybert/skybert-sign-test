# Makefile for the Sign CLI app
#
# Author: torstein@skybert.net

all: format compile run

# Compile app
compile:
	mvn clean
	mvn install

# Run app
run:
	mvn exec:java -Dexec.mainClass="net.skybert.sign.SignApp"


# Format the source code
format:
	google-java-format --replace src/main/java/net/skybert/sign/*.java

# Upgrade 3rd party dependencies except alpha, beta, RC and M
# versions.
upgrade:
	mvn versions:use-latest-releases \
	  -DallowMajorUpdates=true \
	  -Dexcludes='*:*:*:*alpha*,*:*:*:*beta*,*:*:*:*RC*,*:*:*:*M*'

