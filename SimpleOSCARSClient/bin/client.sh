#!/bin/sh 
#
# The client.sh script calls the test client using stdin

# Find where the shell script is being run from
BIN_DIRECTORY="$(cd -- "$(dirname "$0")" && pwd)"
# Set OSCARS_HOME so that other things will work
OSCARS_HOME="$BIN_DIRECTORY/../"
# Set the configuration location path
CONFIG_PATH="$BIN_DIRECTORY/../config"

JAR_FILE="$BIN_DIRECTORY/SimpleOSCARSClient-0.0.1-SNAPSHOT.one-jar.jar"  # the location in the assembled tarball
if [ ! -f "$JAR_FILE" ]; then
    JAR_FILE="$BIN_DIRECTORY/../target/SimpleOSCARSClient-0.0.1-SNAPSHOT.one-jar.jar"    # the location when compiled
fi

# Run the command, setting the config path, and specifying to use stdin
java -Done-jar.verbose=false -Done-jar.info=false -Done-jar.main.class=net.es.oscars.api.client.SimpleOSCARSClient -jar $JAR_FILE -o config_path=$CONFIG_PATH $* 
