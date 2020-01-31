CREATE TYPE idl AS ENUM ('avro', 'protobuf', 'mu', 'openapi', 'scala');

CREATE TABLE metaprotocols (
    id CHARACTER VARYING(255) PRIMARY KEY,
    idl_name idl NOT NULL,
    version INTEGER NOT NULL
);
