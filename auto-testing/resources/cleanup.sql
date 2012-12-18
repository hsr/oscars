USE rm;

select globalReservationId AS 'GRI', status, srcEndpoint as 'src', destEndpoint as 'dst', FROM_UNIXTIME(endTime, '%Y-%m-%d %H:%i:%s') as 'end time' 
from reservations inner join layer2Data 
on reservations.id = layer2Data.id 
WHERE status != 'ACTIVE' AND status != 'FINISHED' AND status != 'CANCELLED' AND status != 'FAILED';

UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'RESERVED';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'ACCEPTED';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'INPATHCALCULATION';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'PATHCALCULATED';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'INCOMMIT';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'COMMITTED';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'INSETUP';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'INTEARDOWN';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'INCANCEL';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'INMODIFY';
UPDATE reservations SET status = 'FAILED', description = 'SET TO FAILED DURING CLEANUP' WHERE status = 'UNKNOWN';

select globalReservationId AS 'GRI', status, srcEndpoint as 'src', destEndpoint as 'dst', FROM_UNIXTIME(endTime, '%Y-%m-%d %H:%i:%s') as 'end time' 
from reservations inner join layer2Data 
on reservations.id = layer2Data.id 
WHERE status != 'ACTIVE' AND status != 'FINISHED' AND status != 'CANCELLED' AND status != 'FAILED';
