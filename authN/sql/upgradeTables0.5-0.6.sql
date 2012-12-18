-- Script to create the authN database and tables and populate them with
-- data from the 0.5 aaa database.
-- If you do not have 0.5 data, use createTable.sql instead.
-- NOTE:  This needs to be run as mysql -u root -p < upgradeTables0.5-0.6.sql.  Can only be run once

CREATE DATABASE IF NOT EXISTS authn;
CREATE DATABASE IF NOT EXISTS testauthn;

USE authn;

CREATE TABLE IF NOT EXISTS users LIKE aaa.users;
#Insert values by regenerating an id number so that no duplicate entries for keys occur
INSERT INTO users (login,
  certIssuer,
  certSubject,
  lastName,
  firstName,
  emailPrimary,
  phonePrimary,
  password,
  description,
  emailSecondary,
  phoneSecondary,
  status,
  activationKey,
  loginTime,
  cookieHash,
  institutionId) 
SELECT login,
  certIssuer,
  certSubject,
  lastName,
  firstName,
  emailPrimary,
  phonePrimary,
  password,
  description,
  emailSecondary,
  phoneSecondary,
  status,
  activationKey,
  loginTime,
  cookieHash,
  institutionId 
	FROM aaa.users;
	# WHERE NOT EXISTS (SELECT login FROM users users_tbl WHERE users_tbl.login=aaa.users.login);
#For new rows not existing in 0.6. The above commented-line would retain current 0.6DB data for duplicate "login" names, but will ensure all non-duplicate data is imported 


CREATE TABLE IF NOT EXISTS userAttributes LIKE aaa.userAttributes;
#Procedure to insert values into userAttributes tables properly 
#This procedure makes sure that the attributes values from the 0.5 userAttributes table
## are copied correctly to the correct user_id of the 0.6 tables 
DELIMITER $$

DROP PROCEDURE IF EXISTS proc_insert_attrs $$
CREATE PROCEDURE proc_insert_attrs()
BEGIN

        DECLARE done INT DEFAULT FALSE;
        DECLARE loop_id, loop_userid, loop_attrid, new_user_id INT(11);
        DECLARE user_login CHAR(12);
        DECLARE getval CURSOR FOR
                SELECT * FROM aaa.userAttributes aaa_attr WHERE aaa_attr.userId IN (SELECT aaa_users.id FROM aaa.users aaa_users, users authn_users WHERE aaa_users.login=authn_users.login);
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  
        OPEN getval;
                read_loop: LOOP
                        SET done = FALSE ;

                        FETCH getval INTO loop_id, loop_userid, loop_attrid;
                        IF done THEN
                                LEAVE read_loop;
                        END IF;
                        select login into user_login from aaa.users users_tbl where users_tbl.id=loop_userid;
                        select users_tbl.id into new_user_id from users users_tbl where users_tbl.login=user_login;
                        INSERT IGNORE INTO userAttributes (userId, attributeId) VALUES (new_user_id, loop_attrid);
                END LOOP;
        CLOSE getval;
END $$
CALL proc_insert_attrs() $$
DELIMITER ;
DROP PROCEDURE proc_insert_attrs;

CREATE TABLE IF NOT EXISTS institutions LIKE aaa.institutions;
#Insert institutions not already present in local institutions table. There is no column that has to be replaced
## in case exisiting data has the same institution, so this is good.
INSERT INTO institutions(name) SELECT name from aaa.institutions WHERE NOT EXISTS (SELECT * FROM institutions WHERE institutions.name = aaa.institutions.name);

CREATE TABLE IF NOT EXISTS attributes LIKE aaa.attributes;
INSERT IGNORE INTO attributes SELECT * from aaa.attributes;

# This procedure will gracefully handle changing column names
DELIMITER $$

DROP PROCEDURE IF EXISTS proc_change_attrscols $$
CREATE PROCEDURE proc_change_attrscols()
BEGIN

IF NOT EXISTS( (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
AND COLUMN_NAME='attrId' AND TABLE_NAME='attributes') )
THEN
   ALTER TABLE attributes CHANGE attrType attrId text not null after id;
END IF;

IF NOT EXISTS( (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
AND COLUMN_NAME='value' AND TABLE_NAME='attributes') )
THEN
   ALTER TABLE attributes CHANGE name value text not null ;
END IF;

END $$

CALL proc_change_attrscols() $$
DELIMITER ;
DROP PROCEDURE proc_change_attrscols;

#ALTER TABLE IF NOT EXISTS attributes CHANGE attrType attrId text not null after id;
#ALTER TABLE attributes CHANGE name value text not null;

