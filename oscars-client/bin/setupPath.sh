#!/bin/sh 
#usage setupPath.sh <gri> sets up the circuit for the  reservation with the id of gri
. bin/setclasspath.sh
java net.es.oscars.client.examples.CreatePath $*

