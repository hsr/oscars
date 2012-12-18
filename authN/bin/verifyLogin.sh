#!/bin/sh 
#usage verifyLogin -u <loginName> -p <password> -C <context>
#  -Djavax.net.debug=all will dump the ssl messages
vers=`cat $OSCARS_DIST/VERSION`
test   ! -d target/tmp  -o \( target/tmp -ot target/api-$vers.one-jar.jar \) && 
. bin/expandOneJar.sh
. bin/setclasspath.sh
java net.es.oscars.authN.test.AuthNTest -c verifyLogin $* 
