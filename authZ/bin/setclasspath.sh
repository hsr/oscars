LIB=target/tmp/lib/
# update classpath
OSCARS_CLASSPATH="target/classes:target/test-classes"
for f in "$LIB"*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done
CLASSPATH=$OSCARS_CLASSPATH
export CLASSPATH=$CLASSPATH
