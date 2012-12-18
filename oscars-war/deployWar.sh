#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
cp $OSCARS_DIST/oscars-war/target/oscars-war-$vers.war $OSCARS_HOME/WBUIService
