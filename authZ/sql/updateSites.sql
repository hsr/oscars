
CREATE DATABASE IF NOT EXISTS authz;
CREATE DATABASE IF NOT EXISTS testauthz;

GRANT select, insert, update, delete ON `authz`.* TO 'oscars'@'localhost';
GRANT select, insert, update, delete, create, drop, alter on `testauthz`.* TO 'oscars'@'localhost';

USE authz;

DROP TABLE sites;
CREATE TABLE IF NOT EXISTS sites (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- topologyId for a domain -- matches topologyIdent in bss domains table
    domainTopologyId    TEXT NOT NULL,
        -- institution matches institution attribute from AuthN
    institutionNam       TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE UNIQUE INDEX row ON sites(domainTopologyId(7),institution(7)); 

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
        INSERT INTO authZ.sites (domainTopologyId,institutionName)
            SELECT topoId,instName;
        SET siteId = siteId +1;
   END WHILE;
END
//
DELIMITER ;

CALL insertSites();
