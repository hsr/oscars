--
-- Table of subscriptions to notifications
--
CREATE TABLE subscriptions (
  id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  referenceId VARCHAR(512) NOT NULL,
  userLogin VARCHAR(256) NOT NULL,
  url VARCHAR(512) NOT NULL,
  createdTime BIGINT NOT NULL,
  terminationTime BIGINT NOT NULL,
  status INT NOT NULL
);
CREATE UNIQUE INDEX subRefIdIndex ON subscriptions (referenceId);

-- Table to hold filters for each subscription
--
CREATE TABLE subscriptionFilters (
  id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  subscriptionId INT NOT NULL,
  type VARCHAR(256) NOT NULL,
  value VARCHAR(1024) NOT NULL
);
CREATE INDEX filtSubIdIndex ON subscriptionFilters (subscriptionId);