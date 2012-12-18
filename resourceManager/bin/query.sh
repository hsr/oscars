#!/bin/sh 
# usage query -gri <gri> ...
#  -Djavax.net.debug=all will dump the ssl messages
test -d target/tmp || . bin/expandOneJar.sh
. bin/setclasspath.sh
java  net.es.oscars.resourceManager.RMServerTest -c query  $* 
