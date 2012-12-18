VERS=`cat $OSCARS_DIST/VERSION`
LIB=target/lib/
# update classpath
OSCARS_CLASSPATH="target/classes:target/oscars-client-$VERS.jar"
for f in "$LIB"*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done
CLASSPATH=$OSCARS_CLASSPATH
export CLASSPATH=$CLASSPATH
