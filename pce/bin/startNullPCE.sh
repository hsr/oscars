#!/bin/bash 
# usage startNullAgg c context
#  -Djavax.net.debug=all will dump the ssl messages
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
test -d target/tmp || . bin/expandOneJar.sh
.  bin/setclasspath.sh
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac
java net.es.oscars.pce.nullpce.NullPCE -c $context & 
echo $! > $DEFAULT_PID_DIR/nullpce.pid
