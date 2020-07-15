ALTER TYPE idl RENAME TO idl_old;

CREATE TYPE idl as ENUM('avro', 'protobuf', 'mu', 'openapi', 'openapijson', 'scala');

ALTER TABLE metaprotocols ALTER COLUMN idl_name TYPE idl USING idl_name::text::idl;

DROP TYPE idl_old;