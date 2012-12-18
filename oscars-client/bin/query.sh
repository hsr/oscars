#!/bin/sh 
#usage .query.sh -h  
. bin/setclasspath.sh
java net.es.oscars.client.examples.QueryReservation $*

