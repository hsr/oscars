use authn;

ALTER TABLE attributes CHANGE attrType attrId text not null after id;
ALTER TABLE attributes CHANGE name value text not null;
