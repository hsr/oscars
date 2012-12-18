#!/bin/sh 
vers=`cat $OSCARS_DIST/VERSION`
rm -rf $OSCARS_DIST/coordinator/target/tmp
mkdir $OSCARS_DIST/coordinator/target/tmp
cp $OSCARS_DIST/coordinator/config/log4j*properties $OSCARS_DIST/coordinator/target/classes
(cd $OSCARS_DIST/coordinator/target/tmp; jar -xf ../coordinator-$vers.one-jar.jar )
