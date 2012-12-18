#!/bin/sh 
#usage create.sh creates a reservation in testdomain-3 and polls for its status
. bin/setclasspath.sh
java net.es.oscars.client.examples.CreateReservationPoll

