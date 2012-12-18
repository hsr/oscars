#!/bin/bash 
# usage query -gri <gri> -u logId -i institution -r <other attributes>
#  -Djavax.net.debug=all will dump the ssl messages
test -d target/tmp || . bin/expandOneJar.sh
.  bin/setclasspath.sh
java net.es.oscars.coord.coordServerTest -c query "$@" 
