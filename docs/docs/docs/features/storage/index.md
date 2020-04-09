---
layout: docs
title: Schema Storage
url: /docs/features/storage
---

# Schema Storage Service

Compendium can work as your schema storage. With only uploading a schema you will be able to download it from everywhere you need it.

# Storing an schema

For storing an schema you just need to do a `POST` request to this endpoint specifying the IDL in which your schema is written:
```
https://compendium-example.com/protocol/myProtocol?idlName=avro
```

# Versioning
All the storaged schemas will be versioned. Currently the versioning is incremental. When a schema is storaged, the version of the schema is returned as a body response.

# Supported IDLs for Stroing
These are the allowed IDLs that can be stored in Compendium so far:

 | IDL      | Supported |
 |----------|-----------|
 | Avro     | Yes       |
 | Protobuf | Yes       |
 | Mu       | No        |
 | OpenAPI  | No        |
