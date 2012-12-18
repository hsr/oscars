#!/bin/sh 
#usage query.sh -gri <gri>
vers=`cat $OSCARS_DIST/VERSION`
test   ! -d target/tmp  -o \( target/tmp -ot target/api-$vers.one-jar.jar \) && . bin/expandOneJar.sh
. bin/setclasspath.sh
# -Djavax.net.debug=all dumps all ssl messages
java net.es.oscars.api.test.IDCTest  \
-v 0.6 -a x509 -c cancelReservation $*

