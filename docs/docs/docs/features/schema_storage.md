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

For more infromation about this endpoint, check the [api](https://???)

The list of IDLs allowd by Compendoum can be found [here](./supported_idls)

# Supported IDLs for Stroing
These are the allowed IDLs that can be stored in Compendium so far:

 | IDL      | Supported |
 |----------|-----------|
 | Avro     | Yes       |
 | Protobuf | Yes       |
 | Mu       | No        |
 | OpenAPI  | No        |
 | Scala    | No        |