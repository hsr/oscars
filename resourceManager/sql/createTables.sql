-- Create the rm and test rm databases, and the empty tables for rm.
-- NOTE:  This needs to be run as mysql -u root -p < createTables.sql.

CREATE DATABASE IF NOT EXISTS rm;
GRANT select, insert, update, delete ON `rm`.* TO 'oscars'@'localhost';

USE rm;

--
-- table holding reservation information
-- this information is used for scheduling
--
CREATE TABLE IF NOT EXISTS reservations (
    id                  INT NOT NULL AUTO_INCREMENT,
    startTime           BIGINT UNSIGNED NOT NULL,
    endTime             BIGINT UNSIGNED NOT NULL,
        -- time this entry was created
    createdTime         BIGINT UNSIGNED NOT NULL,
        -- bandwidth requested (bps)
    bandwidth           BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    login               TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    payloadSender       TEXT,
    status              TEXT NOT NULL,
    localStatus         TINYINT(1) DEFAULT 0,
    description         TEXT,
    statusMessage       TEXT,
    globalReservationId VARCHAR(63) UNIQUE,
PRIMARY KEY (id)
) ENGINE = MyISAM;

--
-- table used as a sequence generator for part of the GRI
--
CREATE TABLE IF NOT EXISTS idSequence (
    id                  INT NOT NULL AUTO_INCREMENT,
PRIMARY KEY (id)
) ENGINE = MyISAM;

--
-- standard constraint table
--
CREATE TABLE IF NOT EXISTS stdConstraints (
    id                  INT NOT NULL AUTO_INCREMENT,
    constraintType      TEXT NOT NULL,
    startTime           BIGINT UNSIGNED NOT NULL,
    endTime             BIGINT UNSIGNED NOT NULL,
    bandwidth           BIGINT UNSIGNED NOT NULL,
    pathId              INT,  -- foreign key
    reservationId       INT NOT NULL,  -- foreign key
    PRIMARY KEY (id)
) ENGINE=MyISAM;

    --
-- optional constraint table
-- bhr 
CREATE TABLE IF NOT EXISTS optConstraints (
    id                  INT NOT NULL AUTO_INCREMENT,
    constraintType      TEXT NOT NULL,
    keyName             TEXT NOT NULL,
    value               TEXT NOT NULL,
    reservationId       INT NOT NULL,  -- foreign key
	seqNumber           INT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

-- topology section

--
-- table for paths associated with pending or active reservations
--
CREATE TABLE IF NOT EXISTS paths (
    id                  INT NOT NULL AUTO_INCREMENT,
    pathSetupMode       TEXT,
    nextDomainId        INT,           -- optional foreign key
    pathType            TEXT,
    direction           TEXT,
    priority            INT,
    grouping            TEXT,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- table for elements in paths associated with pending or active reservations
--
CREATE TABLE IF NOT EXISTS pathElems (
    id                  INT NOT NULL AUTO_INCREMENT,
    pathId              INT NOT NULL, -- foreign key
    seqNumber           INT NOT NULL,
    urn                 TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- table to store additional parameters relating to a pathElem
--
CREATE TABLE IF NOT EXISTS pathElemParams (
    id                  INT NOT NULL AUTO_INCREMENT,
    pathElemId          INT NOT NULL, -- foreign key
    swcap               TEXT NOT NULL,
    type                TEXT NOT NULL,
    value               TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- table for layer 2 information
--
CREATE TABLE IF NOT EXISTS layer2Data (
    id                  INT NOT NULL AUTO_INCREMENT,
    pathId              INT NOT NULL UNIQUE, -- foreign key
    srcEndpoint         TEXT NOT NULL,
    destEndpoint        TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- table for layer 3 information
--
CREATE TABLE IF NOT EXISTS layer3Data (
    id                  INT NOT NULL AUTO_INCREMENT,
    pathId              INT NOT NULL UNIQUE, -- foreign key
    -- the following are optional fields
    srcHost             TEXT,
    destHost            TEXT,
        -- source and destination ports
    srcIpPort           SMALLINT UNSIGNED,
    destIpPort          SMALLINT UNSIGNED,
        -- protocol used (0-255, or a protocol string, such as udp)
    protocol            TEXT,
        -- differentiated services code point
    dscp                TEXT,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- table for MPLS information
--
CREATE TABLE IF NOT EXISTS mplsData (
    id                 INT NOT NULL AUTO_INCREMENT,
    pathId             INT NOT NULL UNIQUE, -- foreign key
        -- in bps
    burstLimit         BIGINT UNSIGNED NOT NULL,
    lspClass           TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- Table for signaling tokens
--
CREATE TABLE IF NOT EXISTS tokens (
  id                 INT NOT NULL AUTO_INCREMENT,
  reservationId      INT NOT NULL,
  value              TEXT NOT NULL,
  PRIMARY KEY  (id)
) ENGINE = MyISAM;

--
-- table for ErrorReports information
--
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
