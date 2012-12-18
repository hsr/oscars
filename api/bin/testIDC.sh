#!/bin/sh 
#usage testIDC.sh  - calls createPath with the token DEADBEEF - should suceed
#     testIDC.sh <token> - calls createPath with input token - should throw an exception
test -d target/tmp || . bin/expandOneJar.sh
. bin/setclasspath.sh
#  -Djavax.net.debug=all will dump the ssl messages
java net.es.oscars.api.test.IDCTest \
0.6 x509 $*

