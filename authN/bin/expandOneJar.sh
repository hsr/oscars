#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
rm -rf $OSCARS_DIST/authN/target/tmp
mkdir $OSCARS_DIST/authN/target/tmp
cp $OSCARS_DIST/authN/config/log4j*properties $OSCARS_DIST/authN/target/classes
(cd $OSCARS_DIST/authN/target/tmp; jar -xf ../authN-$vers.one-jar.jar )
