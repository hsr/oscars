#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
cp $OSCARS_DIST/ion-war/target/ion-war-${vers}.war $OSCARS_HOME/IONUIService
