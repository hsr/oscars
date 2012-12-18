-- remove ConstraintId from paths table
-- add pathId to stdConstraints table

use rm;

alter TABLE stdConstraints ADD COLUMN pathId INT AFTER bandwidth;

DROP PROCEDURE IF EXISTS switchIds;
DELIMITER //
CREATE PROCEDURE switchIds()
BEGIN
    DECLARE pathid INT;
    DECLARE maxpath INT;
    DECLARE resid INT;
    DECLARE consid INT;
    SELECT MIN(id) into pathid FROM paths;
    SELECT MAX(id) into maxpath FROM paths;
    
    WHILE pathid <= maxpath DO
       SELECT constraintId FROM paths WHERE id=pathid INTO consid;
       UPDATE stdConstraints SET pathId=pathid WHERE id=consid;
       SET pathid = pathid +1;
    END WHILE;
END
//
DELIMITER ;

CALL switchIds ();

alter TABLE paths DROP constraintId;
-- 0.5 didn't store a strict or loose value.
ALTER TABLE paths MODIFY COLUMN pathType TEXT;
UPDATE paths SET pathType=null;
