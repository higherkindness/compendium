CREATE TYPE idl AS ENUM ('avro', 'protobuf', 'mu', 'openapi', 'scala');

CREATE TABLE metaprotocols (
    id CHARACTER VARYING(255) PRIMARY KEY,
    idl_name idl NOT NULL,
    version VARCHAR(20) NOT NULL CHECK (version ~ '^(\d+\.)?(\d+\.)?(\*|\d+)$')
);
