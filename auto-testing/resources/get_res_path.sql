#!/bin/bash

# get endpoints by gri

QUERY="SELECT reservations.id, globalReservationId, srcEndpoint, destEndpoint FROM reservations INNER JOIN layer2Data ON reservations.id = layer2Data.id AND globalReservationId = \"$1\""

mysql -u oscars -pmypass rm<<EOF
$QUERY
EOF

