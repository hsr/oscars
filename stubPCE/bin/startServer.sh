#!/bin/sh 
#-Djavax.net.debug=ssl:handshake will dump all the ssl messages
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
java  -Djava.net.preferIPv4Stack=true -jar $OSCARS_DIST/stubPCE/target/stubPCE-$vers.one-jar.jar  &
echo $! > $DEFAULT_PID_DIR/stubPCE.pid
