-- populate the authZ table with defaults values. 
-- the attributes table must match the values in authN attributes
-- the values in sites should match those in authN:institutions
-- the permission, resources and constraints values must match the code
-- the authorization values are defaults and can be changed 
-- NOTE:  This needs to be run as mysql -u root -p < populateDefaults.sql. 


USE authz;

-- populate sites table
-- topologyId for a domain -- matches domain ids in rm pathElems urn field
-- institution matches institution attribute from AuthN
INSERT IGNORE INTO sites VALUES ( NULL,"es.net", "Energy Sciences Network");
INSERT IGNORE INTO sites VALUES ( NULL,"dev.es.net", "Energy Sciences Network");
INSERT IGNORE INTO sites VALUES (NULL, "dcn.internet2.edu","Internet2");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-1","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-2","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-3","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-4","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-1.net","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-2.net","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-3.net","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-4.net","Testing");
INSERT IGNORE INTO sites VALUES (NULL, "testdomain-1-1.net","Testing");

-- populate resources table
INSERT IGNORE INTO resources VALUES(NULL, "Users",
                        "Information about all users", NULL);
INSERT IGNORE INTO resources VALUES(NULL, "Reservations",
                        "Information about all reservations", NULL);
INSERT IGNORE INTO resources VALUES(NULL, "Domains",
                        "Information about OSCARS-realm domain controllers", NULL);
INSERT IGNORE INTO resources VALUES(NULL, "AAA",
                        "Information about Institutions, Attributes and Authorizations", NULL);
INSERT IGNORE INTO resources VALUES(NULL, "Subscriptions",
                        "Information about the relationship between the producer and consumer of notifications",
                        NULL);
                        
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

-- populate permissions table
INSERT IGNORE INTO permissions VALUES(NULL, "list",
            "view minimum information about a user or reservation", NULL);
INSERT IGNORE INTO permissions VALUES(NULL, "query",
            "view complete information about a user or reservation", NULL);
INSERT IGNORE INTO permissions VALUES(NULL, "modify",
            "change or delete a user or reservation", NULL);
INSERT IGNORE INTO permissions VALUES(NULL, "create",
            "create a user or reservation", NULL);
INSERT IGNORE INTO permissions VALUES(NULL, "sendIDCEvent",
            "send an interDomainEvent", NULL);
INSERT IGNORE INTO permissions VALUES(NULL, "publishTo",
            "send an Notification to the NotificationBroker", NULL);
INSERT IGNORE INTO permissions VALUES(NULL, "signal",
            "signal a previously placed reservation", NULL);
            
-- populate constraints table
INSERT IGNORE INTO constraints VALUES (NULL, "none", "", "");
INSERT IGNORE INTO constraints VALUES (NULL, "all-users", "boolean","allows access to reservations or details of all users");
INSERT IGNORE INTO constraints VALUES (NULL, "max-bandwidth", "numeric", "limits reservations to specified bandwidth");
INSERT IGNORE INTO constraints VALUES (NULL, "max-duration", "numeric","limits reservations to specified duration");
INSERT IGNORE INTO constraints VALUES (NULL, "my-site", "boolean", "limits access to reservations to those starting or ending at user's site");
INSERT IGNORE INTO constraints VALUES (NULL, "mysite-in-resv", "boolean", "limits access to reservations where user's site is included in path");
INSERT IGNORE INTO constraints VALUES (NULL, "specify-path-elements", "boolean", "allows path elements to be specified for reservations");
INSERT IGNORE INTO constraints VALUES (NULL, "see-all-hops", "boolean", "allows the internal hops to be seen");
INSERT IGNORE INTO constraints VALUES (NULL, "specify-gri", "boolean", "allows a gri to be specified on path creation");
INSERT IGNORE INTO constraints VALUES (NULL, "unsafe-allowed", "boolean", "allows arbitrary changes to reservation status, to permit database corrections");


-- none constraint
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="list"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="query"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="list"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="query"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="create"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="signal"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="sendIDCEvent"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="sendIDCEvent"),
    (select id from constraints where name="mysite-in-resv"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="Subscriptions"),
    (select id from permissions where name="create"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="Subscriptions"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="AAA"),
    (select id from permissions where name="list"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="AAA"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="Domains"),
    (select id from permissions where name="query"),
    (select id from constraints where name="none"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="Domains"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="none"));

-- all-users constraint
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="create"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="list"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="query"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="list"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="query"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="signal"),
    (select id from constraints where name="all-users"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="Subscriptions"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="all-users"));

--  my-site constraint
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="list"),
    (select id from constraints where name="my-site"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="query"),
    (select id from constraints where name="my-site"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="my-site"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="signal"),
    (select id from constraints where name="my-site"));
    
-- max-bandwidth    
 INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="create"),
    (select id from constraints where name="max-bandwidth"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="max-bandwidth"));
-- max-duration
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="create"),
    (select id from constraints where name="max-duration"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="max-duration"));    
-- specify-path-elements
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="create"),
    (select id from constraints where name="specify-path-elements"));
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="specify-path-elements"));
-- see-all-hops
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="query"),
    (select id from constraints where name="see-all-hops"));
INSERT IGNORE INTO rpcs VALUES (NULL, 
    (select id from resources where name="reservations"),
    (select id from permissions where name="list"),
    (select id from constraints where name="see-all-hops"));
-- specify-gri
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="create"),
    (select id from constraints where name="specify-gri"));
-- allow-unsafe
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="reservations"),
    (select id from permissions where name="modify"),
    (select id from constraints where name="unsafe-allowed"));

-- populate authorizations table
-- authorizations for OSCARS-user
-- query and modify own profile
-- list, query, modify, create and signal own reservations
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="see-all-hops"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="none"),NULL);   
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="create"),
     (select id from constraints where name="none"),NULL); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-user"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL); 
     
-- authorizations for OSCARS-engineer
-- query and modify own profile
-- list,query,modify, create and signal all reservations
-- see internal hops for all reservations
-- when creating or modifying reservations may set path elements
-- query and modify topology information
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
-- super-user authorizations for BSS operations
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="specify-path-elements"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="specify-path-elements"), "true");  
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="see-all-hops"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="see-all-hops"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="unsafe-allowed"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="domains"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="create"),
     (select id from constraints where name="none"),NULL); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-engineer"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
     
--  Authorizations for OSCARS-administrator
-- list, query, create and modify all user information
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="create"),
     (select id from constraints where name="all-users"),"true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="list"),
     (select id from constraints where name="none"),NULL); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);

INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-administrator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), "true");

-- authorizations for an IDC forwarding a request to an adjacent domain 
-- note that all the reservations fowarded by a IDC are owned by the IDC
-- Query, modify, list, signal reservations that it owns
-- sendIDCEvent for reservations that include their domain
-- Create reservations specifying GRI and path elements
-- Fetch topology, modify local topology
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="my-site"),"true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL); 
-- list is only used in debugging interdomain interactions
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="none"),NULL); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="specify-path-elements"), "true"); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="specify-gri"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="specify-path-elements"), "true");  
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="sendIDCEvent"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="create"),
     (select id from constraints where name="none"),NULL); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL); 
-- maybe constraint should be "mysite-in-resv"
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-service"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="publishTo"),
     (select id from constraints where name="none"),NULL);
     
-- NOC operators
-- List and query all reservations
-- List all users
-- See and modify own profile
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), "true"); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), "true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);      
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-operator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="create"),
     (select id from constraints where name="none"),NULL); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-operator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
     
    -- Site Administrator
    -- list and query his own profile
    -- List, query, modify, create and signal any reservation
    --   that starts or terminates at his site
 INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="my-site"),"true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="my-site"),"true"); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="my-site"),"true"); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="my-site"),"true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     (select id from constraints where name="my-site"),"true");
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="create"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-site-administrator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);

    -- Publisher
    -- Publish notifications    
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-publisher"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="publishTo"),
     (select id from constraints where name="none"),NULL);

 -- OSCARS-may-specify-path
 INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-may-specify-path"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="specify-path-elements"), "true");  
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="OSCARS-may-specify-path"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="specify-path-elements"), "true");  
