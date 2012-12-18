#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
rm -rf $OSCARS_DIST/resourceManager/target/tmp
mkdir $OSCARS_DIST/resourceManager/target/tmp
cp $OSCARS_DIST/resourceManager/config/log4j*properties $OSCARS_DIST/resourceManager/target/classes
cp $OSCARS_DIST/resourceManager/src/test/resources/store.yaml $OSCARS_DIST/resourceManager/target/classes
(cd $OSCARS_DIST/resourceManager/target/tmp; jar -xf ../resourceManager-$vers.one-jar.jar )
