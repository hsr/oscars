-- Populate the AuthN tables with default values 

USE authn;

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

