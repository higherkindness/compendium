---
layout: docs
title: Transformations
url: /docs/features/translations
---

# Translations
Compendion not oly can be used as a schema storage, but a schema translator. Compendium is able to translate from one IDL to another. So, as an instance, if you have a schema defined in Avro, but you need it in Mu, Compendium will take care of translate it for you.

# Translating a Schema
Translate an schema can not be more easier now. The schema must be stroaged before being translated, to know how to store a schema visit [this page](./schema_storafe.md).

Now the schema exists in the storage, you just need to run a `GET` request to this endpoint:
```
https://compendium-example.com/protocol/myProtocol/transformation?target=mu&version=0.0.1
```

The result will be the schema translated to mu, in it's version 0.0.1

 # Supported IDLs for Transformation
 In this matrix you can see all the allowd transformations by Compendium so far:

 | From/To  | Avro | Protobuf | Mu  | OpenAPI | Scala |
 |----------|------|----------|-----|---------|-------|
 | Avro     | -    | No       | Yes | No      | No    |
 | Protobuf | No   | -        | Yes | No      | No    |
 | Mu       | No   | No       | -   | No      | No    |
 | OpenAPI  | No   | No       | No  | -       | No    |
 | Scala    | No   | No       | No  | No      | -     |