---
layout: docs
title: sbt plugin
url: /docs/sbt
---

# What is `sbt-compendium`?

`sbt-compendium` is a plugin that provides a client to communicate with [Compendium](https://github.com/higherkindness/compendium) service. It currently support:


- Retrieval of protocols


To start using `sbt-compendium`, add the following line in `project/plugin.sbt`:

```
addSbtPlugin("io.higherkindness" %% "sbt-compendium" % <version>)
```

## Available settings

| Setting | Type | Description | Default value |
|---|---|---|---|
| `compendiumSrcGenServerHost` | _String_ | Compendium server host | `localhost` |
| `compendiumSrcGenServerPort` | _Integer_ | Compendium server port | `47047` |
| `compendiumSrcGenFormatSchema` | _IdlName_ | Schema type to download | `IdlName.Avro` |
| `compendiumSrcGenProtocolIdentifiers` | _Seq[ProtocolAndVersion]_ | Protocol identifiers to retrieve from compendium. `ProtocolAndVersion` provides two values: `name` (mandatory) that corresponds with the identifier used to store the protocol and `version` (optional) | `Nil` |

The configuration works as follows:

-  `compendiumSrcGenServerHost`: *String*. Url of the compendium server.
   Default value: "localhost"
-  `compendiumSrcGenServerPort`: *Integer*. Port of the compendium
   server. Default value: 47047
-  `compendiumSrcGenFormatSchema`: *IdlName type*. Schema type to
   download. Default value: IdlName.Avro. Currently supported: Avro,
   Proto.
-  `compendiumSrcGenProtocolIdentifiers`: *Seq[ProtocolAndVersion]*.
   Protocol identifiers to be retrieved from the compendium server.
   `ProtocolAndVersion` provides two values: `name` (mandatory) that
   corresponds with the identifier used to store the protocol and
   `version` (optional). Default: Nil


## How to use it

Once you have set up the configuration, `sbt-compendium` will, during compilation,
get the protocols from the compendium service, validate them and create the
proper Scala classes in `target/scala-2.12/src_managed`.

## Example

An example of sbt-compendium usage is available at [compendium-example](https://github.com/higherkindness/compendium-example). It looks like:


```mdoc
    .settings(
         compendiumSrcGenProtocolIdentifiers := List(ProtocolAndVersion("supplier",None),ProtocolAndVersion("material",None),ProtocolAndVersion("sale",None)),
         compendiumSrcGenServerHost := "localhost",
         compendiumSrcGenServerPort := 8080,
         sourceGenerators in Compile += Def.task {
           compendiumSrcGenClients.value
         }.taskValue
    )
```
