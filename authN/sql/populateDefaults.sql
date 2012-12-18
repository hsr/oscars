-- Populate the AuthN tables with default values 
-- The institutions values need to match the sites table in authz

USE authn;

-- populate institutions table     
INSERT IGNORE INTO institutions VALUES(1, "Energy Sciences Network");
INSERT IGNORE INTO institutions VALUES(2, "Internet2");
INSERT IGNORE INTO institutions VALUES(3, "Testing");

-- populate attributes table
-- ordinary OSCARS user
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-user", "role" , "make reservations");

-- member of the  network engineering group. Has complete control over
-- all reservations
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-engineer", "role", "manage all reservations, view and update topology");

-- Has complete control over all user accounts, including granting permissions
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-administrator", "role", "manage all users");

-- attribute for an IDC in an adjacent network domain. It's attributes implement
-- an SLA between domains.  Currently set to all permissions on reservations and 
-- query permissions for domains, no permissions on users
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-service", "role", "make reservations and view topology");

-- for use by NOC operators. Can see all reservations.
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-operator", "role", "view all reservations");

-- Site Administrator - Can manage all reservations starting or terminating at a site
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-site-administrator", "role", "manage all reservations starting or ending at site");

-- Publisher - for use by IDCs and other services that want to publish notifications
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-publisher", "role",
                        "publish events to external services");
                        
-- OSCARS-may-specify-path - an attribute that can be given to any user in addition to
--    a normal OSCARS-user role that allows specification of path elements on create reservation
INSERT IGNORE INTO attributes VALUES (NULL, "OSCARS-may-specify-path", "privilege",
                         "an add-on attribute to allow specification of path elements");


-- add two test users to the users table Should be removed before production use
INSERT IGNORE INTO users VALUES(null,"client","CN=TheRA, OU=NOT FOR PRODUCTION, O=ESNET, ST=CA, C=US",
                    "CN=Client, OU=NOT FOR PRODUCTION, O=ESNET, ST=CA, C=US","Client","The","client@institution",
                    "555-1212",NULL,"Test user",NULL,NULL,NULL,NULL,NULL,NULL,
                    (select id from institutions where name="Testing"));
INSERT IGNORE INTO users VALUES(null,"IDCService","CN=TheRA, OU=NOT FOR PRODUCTION, O=ESNET, ST=CA, C=US",
                    "CN=IDCService, OU=NOT FOR PRODUCTION, O=ESNET, ST=CA, C=US","Service","IDC","idcServicet@institution",
                    "555-1212",NULL,"Test IDCEService",NULL,NULL,NULL,NULL,NULL,NULL,
                    (select id from institutions where name="Testing"));

-- grant the client user attribute of OSCARS-engineer
INSERT IGNORE INTO userAttributes VALUES (null,
                                         (select id from users where login="client"),
                                         (select id from attributes where value="OSCARS-engineer"));
-- grant the client the ability to send notifications for testing
INSERT IGNORE INTO userAttributes VALUES (null,
                                         (select id from users where login="client"),
                                         (select id from attributes where value="OSCARS-publisher"));
-- grant the client user attribute of OSCARS-service so that it can be used for forward inter-domain reservations
-- this should be removed when to messages get forwarded by the IDCService user
INSERT IGNORE INTO userAttributes VALUES (null,
                                         (select id from users where login="client"),
                                         (select id from attributes where value="OSCARS-service"));
-- grant the IDCService the ability to publish notifications
INSERT IGNORE INTO userAttributes VALUES (null,
                                         (select id from users where login="IDCService"),
                                         (select id from attributes where value="OSCARS-publisher"));
-- grant the IDCServie user attribute of OSCARS-service
INSERT IGNORE INTO userAttributes VALUES (null,
                                         (select id from users where login="IDCService"),
                                         (select id from attributes where value="OSCARS-service"));


