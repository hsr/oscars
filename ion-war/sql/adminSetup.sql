use authn;

#attributes table has changes:attrType changed to attrId not null after id
## name changed to name text not null
INSERT IGNORE INTO attributes VALUES(NULL, 'ION-administrator', 'role', 'manage all users added by your organization');
INSERT IGNORE INTO attributes VALUES(NULL, 'ION-operator', 'role', 'view site reservations');     


use authz;
INSERT IGNORE INTO attributes VALUES(NULL, 'ION-administrator', 'role', 'manage all users added by your organization');
INSERT IGNORE INTO attributes VALUES(NULL, 'ION-operator', 'role', 'view site reservations');

INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
     
#INSERT IGNORE INTO attributes VALUES(NULL, 'ION-operator', 'role', 'view site reservations');     
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="my-site"),"true"); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="my-site"),"true"); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-operator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="create"),
     (select id from constraints where name="none"),NULL);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where value="ION-operator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);
