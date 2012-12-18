CREATE DATABASE IF NOT EXISTS ion;
GRANT select, insert, update, delete ON `ion`.* TO 'oscars'@'localhost';

USE ion;

#CREATE TABLE favorites (
#    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, 
#    login VARCHAR(100) NOT NULL, 
#    gri VARCHAR(100) NOT NULL
#);
#CREATE INDEX loginIndex ON favorites(login);
#CREATE UNIQUE INDEX loginGRIIndex ON favorites(login, gri);
CREATE TABLE IF NOT EXISTS favorites (
	id 		INT NOT NULL AUTO_INCREMENT,
	login		VARCHAR(100) NOT NULL,
	gri 		VARCHAR(100) NOT NULL,
	PRIMARY KEY (id)
) ENGINE=MyISAM;
#create_index_if_not_exists('favorites','loginIndex','login');
#create index will throw a warning if already existing.
CREATE INDEX loginIndex ON favorites(login);
CREATE UNIQUE INDEX loginGRIIndex ON favorites(login, gri);



#CREATE TABLE endpoints (
#    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, 
#    gri VARCHAR(100) NOT NULL, 
#    source VARCHAR(150), 
#    destination VARCHAR(150)
#);
#CREATE UNIQUE INDEX griIndex ON endpoints (gri);
CREATE TABLE IF NOT EXISTS endpoints (
	id 		INT NOT NULL AUTO_INCREMENT,
	gri 	VARCHAR(100) NOT NULL,
	source 	VARCHAR(150),
	destination VARCHAR(150),
	PRIMARY KEY (id)
) ENGINE=MyISAM;
CREATE UNIQUE INDEX griIndex ON endpoints (gri);


#CREATE TABLE adminOrganizationUsers (
#    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, 
#    organization VARCHAR(100) NOT NULL, 
#    username VARCHAR(100)
#);
#CREATE INDEX organizationIndex ON adminOrganizationUsers(organization);

CREATE TABLE IF NOT EXISTS adminOrganizationUsers (
	id 		INT NOT NULL AUTO_INCREMENT,
	organization 	VARCHAR(100) NOT NULL,
	username 	VARCHAR(100),
	PRIMARY KEY (id)
) ENGINE=MyISAM;
CREATE INDEX organizationIndex ON adminOrganizationUsers(organization);
