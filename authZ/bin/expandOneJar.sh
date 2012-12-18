#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
rm -rf  $OSCARS_DIST/authZ/target/tmp
mkdir  $OSCARS_DIST/authZ/target/tmp
cp  $OSCARS_DIST/authZ/config/log4j.*.properties  $OSCARS_DIST/authZ/target/classes
(cd $OSCARS_DIST/authZ/target/tmp; jar -xf ../authZ-$vers.one-jar.jar )
