-- Script to create the AuthZ database and tables and populate them with
-- data from the 0.5 aaa database.
-- If you do not have 0.5 data, use createTable.sql instead.
-- NOTE:  This needs to be run as mysql -u root -p < upgradeTables0.5-0.6.sql.  Can only be run once.

CREATE DATABASE IF NOT EXISTS authz;
CREATE DATABASE IF NOT EXISTS testauthz;

USE authz;

CREATE TABLE authorizations LIKE aaa.authorizations;
INSERT INTO authorizations SELECT * from aaa.authorizations;

CREATE TABLE attributes LIKE aaa.attributes;
INSERT INTO attributes SELECT * from aaa.attributes;

CREATE TABLE resources LIKE aaa.resources;
INSERT INTO resources SELECT * from aaa.resources;

CREATE TABLE permissions LIKE aaa.permissions;
INSERT INTO permissions SELECT * from aaa.permissions;

CREATE TABLE constraints LIKE aaa.constraints;
INSERT INTO constraints SELECT * from aaa.constraints;

CREATE TABLE rpcs LIKE aaa.rpcs;
INSERT INTO rpcs SELECT * from aaa.rpcs;

-- Table to look up an institution associated with a domain (for site admin
-- privileges)

CREATE TABLE IF NOT EXISTS sites (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- topologyId for a domain -- matches topologyIdent in bss domains table
    domainTopologyId    TEXT NOT NULL,
        -- institution matches institution attribute from AuthN
    institutionName     TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE UNIQUE INDEX row ON sites(domainTopologyId(7),institutionName(30));

ALTER TABLE attributes CHANGE attrType attrId text not null after id;
ALTER TABLE attributes CHANGE name value text not null;

DROP PROCEDURE IF EXISTS insertSites;
DELIMITER //
CREATE PROCEDURE insertSites()
BEGIN
    DECLARE siteId INT;
    DECLARE maxSiteId INT;
    DECLARE instId INT;
    DECLARE instName TEXT;
    DECLARE topoId TEXT;
    SELECT MIN(id) into siteId FROM aaa.sites;
    SELECT MAX(id) INTO maxSiteId FROM aaa.sites;
    WHILE siteId <= maxSiteId DO
        SELECT domainTopologyId,institution FROM aaa.sites WHERE id=siteID INTO topoId,instId;
        SELECT name FROM aaa.institutions WHERE id=instId INTO instName;
        INSERT INTO authz.sites (domainTopologyId,institutionName)
            SELECT topoId,instName;
        SET siteId = siteId +1;
   END WHILE;
END
//
DELIMITER ;

CALL insertSites();
