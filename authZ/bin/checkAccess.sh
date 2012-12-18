#!/bin/sh 
# usage checkaccess -r <resource> -p <permission> -a <attribute> -a <attribute> ...
# assumes requested action is list
#  -Djavax.net.debug=all will dump the ssl messages
test -d target/tmp || . bin/expandOneJar.sh
. bin/setclasspath.sh
java net.es.oscars.authZ.test.AuthZTest -c checkAccess $* 
