-- Creates the authz Database and tables: sites,resources,permissions,
-- constraints, rpc, and attributes.
-- NOTE:  This needs to be run as mysql -u root -p < createTables.sql.

CREATE DATABASE IF NOT EXISTS authz;
GRANT select, insert, update, delete ON `authz`.* TO 'oscars'@'localhost';

USE authz;

-- AUTHZ tables ------------------------------------------------------

-- Table to look up an institution associated with a domain (for site admin
-- privileges)

CREATE TABLE IF NOT EXISTS sites (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- topologyId for a domain -- matches domain id in rm pathElems urn field
    domainTopologyId    TEXT NOT NULL,
        -- institution matches institution attribute from AuthN
    institutionName     TEXT NOT NULL, UNIQUE INDEX USING BTREE(domainTopologyId(15),institutionName(30)),
    PRIMARY KEY (id)
) ENGINE=MyISAM;

-- CREATE UNIQUE INDEX row ON sites(domainTopologyId(15),institutionName(30));

CREATE TABLE IF NOT EXISTS resources (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL, UNIQUE INDEX USING BTREE (name(10)),
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS permissions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,UNIQUE INDEX USING BTREE (name(10)),
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) ENGINE=MyISAM;


-- Create constraints table for use of the AUTHZ web interface
CREATE TABLE IF NOT EXISTS constraints (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL, UNIQUE INDEX USING BTREE (name(10)),
    type                TEXT NOT NULL,
    description         TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
-- CREATE UNIQUE INDEX constraintName ON constraints(name(9));

-- Create resource, permission, constraint (RPC) table which contains a list of the meaningful RPC tuples
CREATE TABLE IF NOT EXISTS rpcs (
   id                   INT NOT NULL AUTO_INCREMENT,
   resourceId           INT NOT NULL,
   permissionId         INT NOT NULL,
   constraintId         INT NOT NULL, UNIQUE INDEX USING BTREE (resourceId,permissionId,constraintId),
   PRIMARY KEY (id)
) ENGINE=MyISAM;
-- CREATE UNIQUE INDEX const ON rpcs(resourceId,permissionId,constraintId);


CREATE TABLE IF NOT EXISTS attributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    value               TEXT NOT NULL, UNIQUE INDEX USING BTREE (value(15)),
    attrId              TEXT NOT NULL,
    description         TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
-- CREATE UNIQUE INDEX attrName ON attributes(value(15));

CREATE TABLE IF NOT EXISTS authorizations (
    id                  INT NOT NULL AUTO_INCREMENT,
    context             TEXT,
    updateTime          BIGINT,
    attrId              INT NOT NULL,    -- foreign key
    resourceId          INT NOT NULL,    -- foreign key
    permissionId        INT NOT NULL,    -- foreign key
    constraintId        INT NOT NULL, UNIQUE INDEX USING BTREE (attrId,resourceId,permissionId,constraintId),
    constraintValue     TEXT,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
-- CREATE UNIQUE INDEX row ON authorizations (attrId,resourceId,permissionId,constraintId);

