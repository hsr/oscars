GRANT select, insert, update, delete ON authz.* TO 'oscars'@'localhost' IDENTIFIED BY 'mypass';
GRANT select, insert, update, delete, create, drop, alter on `testauthz`.* TO 'oscars'@'localhost' IDENTIFIED BY 'mypass';
