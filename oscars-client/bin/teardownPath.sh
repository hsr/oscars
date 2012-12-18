#!/bin/sh 
#usage teardownPath.sh <gri> tears down the path of an ACTIVE reservation with the id of gri
. bin/setclasspath.sh
java net.es.oscars.client.examples.TeardownPath $*

