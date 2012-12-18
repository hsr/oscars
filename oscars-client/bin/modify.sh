#!/bin/sh 
#usage modify.sh <gri> extends the time of the reservation by 1 hour
. bin/setclasspath.sh
java net.es.oscars.client.examples.ModifyReservation $*

