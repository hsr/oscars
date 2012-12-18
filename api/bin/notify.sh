#!/bin/sh 
#usage notify.sh  - calls IDCTest -c notify  should succeed
vers=`cat $OSCARS_DIST/VERSION`
test   ! -d target/tmp  -o \( target/tmp -ot target/api-$vers.one-jar.jar \) && . bin/expandOneJar.sh
. bin/setclasspath.sh
#  -Djavax.net.debug=all will dump the ssl messages
java net.es.oscars.api.test.IDCTest \
-v 0.6 -a x509 -c notify $*

