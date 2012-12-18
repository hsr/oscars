CREATE TABLE services (
    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type VARCHAR(100) NOT NULL,
    lastUpdated BIGINT NOT NULL
);
CREATE INDEX servTypeIndex ON services (type);

CREATE TABLE protocols (
    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    serviceId INT NOT NULL,
    protocol VARCHAR(300) NOT NULL,
    url VARCHAR(300) NOT NULL
);
CREATE UNIQUE INDEX serviceProtoIndex ON protocols (serviceId, protocol);

CREATE TABLE relationships (
    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    serviceId INT NOT NULL,
    type VARCHAR(100) NOT NULL,
    relatedTo VARCHAR(300) NOT NULL
);
CREATE INDEX relServiceTypeIndex ON relationships (serviceId, type);

CREATE TABLE registrations (
    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    regElement VARCHAR(300) NOT NULL,
    serviceUrl VARCHAR(300) NOT NULL,
    registrationKey VARCHAR(300)
);

