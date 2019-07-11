CREATE TYPE idl AS ENUM ('avro', 'protobuf', 'mu', 'openapi', 'scala');

CREATE TABLE protocols (
    id VARCHAR PRIMARY KEY,
    idl_name idl NOT NULL
);
