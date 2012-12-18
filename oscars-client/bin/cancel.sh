#!/bin/sh 
#usage cancel.sh <gri> cancels the reservation with the id of gri
. bin/setclasspath.sh
java net.es.oscars.client.examples.CancelReservation $*

