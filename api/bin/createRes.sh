#!/bin/sh   
#usage createRes.sh [-gri <gri>] [-pf <paramFile>]
vers=`cat $OSCARS_DIST/VERSION`
test   ! -d target/tmp  -o \( target/tmp -ot target/api-$vers.one-jar.jar \) && . bin/expandOneJar.sh
. bin/setclasspath.sh
cp src/test/resources/*.yaml target/test-classes
# -Djavax.net.debug=all dumps all ssl messages
java net.es.oscars.api.test.IDCTest  \
-v 0.6 -a x509 -c createReservation $*

