-- Creates the rm database and tables and populates it with data from the 0.5 bss database.
-- If you don't have 0.5 database use creataeAnonTables.sql or createTables.sql instead.
-- NOTE:  This needs to be run as mysql -u root -p < upgradeTables0.5-0.6.sql.  Can only be run once.

CREATE DATABASE IF NOT EXISTS rm;
CREATE DATABASE IF NOT EXISTS testrm;

USE rm;

--
-- standard constraint table
--
DROP TABLE IF EXISTS stdConstraints;
CREATE TABLE stdConstraints (
    id                  INT NOT NULL AUTO_INCREMENT,
    constraintType      TEXT NOT NULL,
    startTime           BIGINT UNSIGNED NOT NULL,
    endTime             BIGINT UNSIGNED NOT NULL,
    bandwidth           BIGINT UNSIGNED NOT NULL,
    pathId              INT,  -- foreign key
    reservationId       INT NOT NULL,  -- foreign key
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX consType ON stdConstraints ( constraintType(4),reservationId);
--
-- optional constraint table
--
CREATE TABLE IF NOT EXISTS optConstraints (
    id                  INT NOT NULL AUTO_INCREMENT,
    constraintType      TEXT NOT NULL,
    keyName             TEXT NOT NULL,
    value               TEXT NOT NULL,
    reservationId       INT NOT NULL,  -- foreign key
	seqNumber           INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS errorReports (
    id                  INT NOT NULL AUTO_INCREMENT,
    reservationId       INT NOT NULL, -- foreign key
    seqNumber           INT NOT NULL,
    errorCode           TEXT NOT NULL,
    errorMsg            TEXT NOT NULL,
    errorType           TEXT NOT NULL,
    GRI                 TEXT,
    transId             TEXT,
    timestamp           BIGINT,
    moduleName          TEXT,
    domainId            TEXT,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

DROP TABLE IF EXISTS reservations;
CREATE TABLE reservations LIKE bss.reservations;
INSERT INTO reservations SELECT * from bss.reservations;

DROP TABLE IF EXISTS paths;
CREATE TABLE paths LIKE bss.paths;
-- pathType is now nullable and equal to strict or loose
ALTER TABLE paths MODIFY COLUMN pathType TEXT;
INSERT INTO paths SELECT * from bss.paths;

DROP TABLE IF EXISTS idSequence;
CREATE TABLE idSequence LIKE bss.idSequence;
INSERT INTO idSequence SELECT * from bss.idSequence;

DROP TABLE IF EXISTS tokens;
CREATE TABLE tokens LIKE bss.tokens;
INSERT INTO tokens SELECT * from bss.tokens;

DROP TABLE IF EXISTS pathElems;
CREATE TABLE pathElems LIKE bss.pathElems;
INSERT INTO pathElems SELECT * from bss.pathElems;

DROP TABLE IF EXISTS pathElemParams;
CREATE TABLE pathElemParams LIKE bss.pathElemParams;
INSERT INTO pathElemParams SELECT * from bss.pathElemParams;

DROP TABLE IF EXISTS layer2Data;
CREATE TABLE layer2Data LIKE bss.layer2Data;
INSERT INTO layer2Data SELECT * from bss.layer2Data;

DROP TABLE IF EXISTS layer3Data;
CREATE TABLE  layer3Data LIKE bss.layer3Data;
INSERT INTO layer3Data SELECT * from bss.layer3Data;

DROP TABLE IF EXISTS mplsData;
CREATE TABLE mplsData LIKE bss.mplsData;
INSERT INTO mplsData SELECT * from bss.mplsData;

ALTER TABLE pathElems DROP linkId;
ALTER TABLE pathElems DROP userName;

DROP PROCEDURE IF EXISTS insertConstraints;
DELIMITER //
CREATE PROCEDURE insertConstraints()
BEGIN
    DECLARE pathid INT;
    DECLARE maxpath INT;
    DECLARE resid INT;
    DECLARE consid INT;
    DECLARE patht TEXT;
    SELECT MIN(id) into pathid FROM paths;
    SELECT MAX(id) into maxpath FROM paths;
    -- create a user constraint for each reservation to confirm to schema
    INSERT INTO stdConstraints (constraintType,startTime,endTime,bandWidth,reservationId)
        SELECT "user", reservations.startTime, reservations.endTime, reservations.bandWidth, reservations.id
        FROM reservations;
    WHILE pathid <= maxpath DO
       SELECT reservationId, pathType  FROM paths where id=pathid INTO resid, patht;
       IF patht = "requested" THEN
          SELECT id from stdConstraints WHERE constraintType="user" && reservationId=resid INTO consid;
          -- the following is just a print statement and can be removed
          SELECT "user", consid, patht, pathid,   resid;
          UPDATE stdConstraints SET pathId=pathid WHERE id=consid;
          END IF;
      -- only create a reserved stdConstraint, if a path has been reserved
      IF patht = "local" || patht = "interdomain" THEN
          INSERT IGNORE INTO stdConstraints (constraintType, startTime,endTime,bandWidth,reservationId)
              SELECT "reserved", reservations.startTime, reservations.endTime, reservations.bandWidth, reservations.id
              FROM reservations WHERE id=resid;
          SELECT id FROM stdConstraints WHERE reservationId=resid && constraintType="reserved" INTO consid;
          -- the following is just a print statement and can be removed
          SELECT "reserved", consid, patht, pathid, resid;
          UPDATE stdConstraints SET pathId=pathid WHERE id=consid;
          END IF;
       SET pathid = pathid +1;
    END WHILE;
END
//
DELIMITER ;
CALL insertConstraints ();

alter TABLE paths DROP reservationId;
-- 0.5 didn't store a strict or loose value.
UPDATE paths SET pathType=null;


