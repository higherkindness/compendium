CREATE TABLE protocols (
    id CHARACTER VARYING(255) NOT NULL,
    version VARCHAR(20) NOT NULL CHECK (version ~ '^(\d+\.)?(\d+\.)?(\*|\d+)$'),
    protocol BYTEA,
    PRIMARY KEY (id, version)
);
