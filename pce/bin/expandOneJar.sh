#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
rm -rf $OSCARS_DIST/pce/target/tmp
mkdir $OSCARS_DIST/pce/target/tmp
cp $OSCARS_DIST/pce/config/log4j*properties $OSCARS_DIST/pce/target/classes
(cd $OSCARS_DIST/pce/target/tmp; jar -xf ../pce-$vers.one-jar.jar )
