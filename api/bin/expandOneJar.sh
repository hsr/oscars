#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
rm -rf $OSCARS_DIST/api/target/tmp
mkdir $OSCARS_DIST/api/target/tmp
cp $OSCARS_DIST/api/config/log4j*properties $OSCARS_DIST/api/target/classes
(cd $OSCARS_DIST/api/target/tmp; jar -xf ../api-$vers.one-jar.jar )
