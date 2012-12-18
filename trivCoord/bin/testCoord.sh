#!/bin/sh 
#usage testCoord
#     testCoord.sh [GRI] - calls createPath with input Will succeed if no
#     GRI is given otherwise should throw an exception
test -d target/tmp || . bin/expandOneJar.sh
. bin/setclasspath.sh
#  -Djavax.net.debug=all will dump the ssl messages
java net.es.oscars.trivCoord.test.CoordCLTest $*
