#!/bin/sh 
# usage checkaccess <resource> <attribute> <attribute> ...
# assumes requested actions are  list, query, create
. bin/setclasspath.sh
test -d target/tmp || bin/expandOneJar.sh
#  -Djavax.net.debug=all will dump the ssl messages
java  net.es.oscars.authZ.test.AuthZTest checkMultiAccess $*
