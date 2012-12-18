#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
rm -rf $OSCARS_DIST/tools/target/tmp
mkdir $OSCARS_DIST/tools/target/tmp
(cd $OSCARS_DIST/tools/target/tmp; jar -xf ../tools-$vers.one-jar.jar )
