-- Creates the authn database and tables.
-- After this script has been run, use populateDefaults to add default values
-- NOTE:  This needs to be run as mysql -u root -p < createTables.sql.

CREATE DATABASE IF NOT EXISTS authn;
GRANT select, insert, update, delete ON `authn`.* TO 'oscars'@'localhost';

USE authn;

-- AUTHN tables ------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id                  INT NOT NULL AUTO_INCREMENT,
    login               TEXT NOT NULL, UNIQUE INDEX USING BTREE(login(20)),
    certIssuer          TEXT,
    certSubject         TEXT,
    lastName            TEXT NOT NULL,
    firstName           TEXT NOT NULL,
    emailPrimary        TEXT NOT NULL,
    phonePrimary        TEXT NOT NULL,
    password            TEXT,
    description         TEXT,
    emailSecondary      TEXT,
    phoneSecondary      TEXT,
    status              TEXT,
    activationKey       TEXT,
    loginTime           BIGINT,
    cookieHash          TEXT,
    institutionId       INT NOT NULL,    -- foreign key (when convert to InnoDB)
    PRIMARY KEY (id)

) ENGINE=MyISAM;


CREATE TABLE IF NOT EXISTS institutions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL, UNIQUE INDEX USING BTREE (name(35)),
    PRIMARY KEY (id)
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS attributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    value               TEXT NOT NULL,UNIQUE INDEX USING BTREE (value(15)),
    attrId              TEXT NOT NULL,
    description		TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

-- cross reference table
CREATE TABLE IF NOT EXISTS userAttributes (
    id		            INT NOT NULL AUTO_INCREMENT,
    userId              INT NOT NULL,    -- foreign key
    attributeId         INT NOT NULL, UNIQUE INDEX USING BTREE(userId,attributeId),   -- foreign key
    PRIMARY KEY (id)
) ENGINE =MyISAM;

