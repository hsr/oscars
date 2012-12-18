#!/bin/sh 
vers=`cat $OSCARS_DIST/VERSION`
test   ! -d target/tmp  -o \( target/tmp -ot target/api-$vers.one-jar.jar \) && . bin/expandOneJar.sh
. bin/setclasspath.sh
#  -Djavax.net.debug=all will dump the ssl messages
java  net.es.oscars.authN.test.AuthNTest -c verifyDN $* \
-s "CN=Mary R. Thompson 483508, OU=People, DC=doegrids, DC=org" \
-i "CN=DOEGrids CA 1, OU=Certificate Authorities, DC=DOEGrids, DC=org"
