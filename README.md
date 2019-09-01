[comment]: # (Start Badges)

[![Build Status](https://travis-ci.org/higherkindness/compendium.svg?branch=master)](https://travis-ci.org/higherkindness/compendium) [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/higherkindness/compendium/master/LICENSE) [![GitHub Issues](https://img.shields.io/github/issues/higherkindness/compendium.svg)](https://github.com/higherkindness/compendium/issues)

[comment]: # (End Badges)

# compendium

## What is compendium?
compendium is a standalone solution, implemented as an HTTP service, that provides storage, conversion and client generation for your schemas in a format-agnostic fashion.

## Features and comparison with Schema Registry

|Feature                          |compendium |Schema Registry|
|-------------------------------  |-----------|---------------|
|RESTful interface                |yes        |yes            |
|Schema storage                   |yes        |yes            |
|Schema versioning                |yes        |yes            |
|Pluggable storage backends       |yes        |no (Kafka)     |
|Format-agnostic                  |yes        |no (Avro)      |
|Conversion between formats       |yes        |no             |
|Client code generation           |yes        |no             |

Pluggable storage backends means that you can use whatever storage fits your needs, being filesystem and PostgreSQL the current out of the box backends provided by compendium.

Format-agnostic ensures that you can use any of the most popular schema formats, ranging from binaries like Avro and Protocol Buffers, to more web related like OpenAPI, in a transparent manner.

Conversion between formats saves a ton of work trying to manually adapt schemas that you are already using, while retaining type safety and ensuring formats compatibility.

Finally, code generation (Scala) is a very interesting feature that allows putting into use all those stored schemas without worrying about their future changes and evolutions.

# Contributing

Please review the [Contribution document](CONTRIBUTING.md) for information on how to get started contributing to the project.

[comment]: # (Start Copyright)
# Copyright

compendium is designed and developed by 47 Degrees

Copyright © 2018-2019 47 Degrees. <http://47deg.com>

Licensed under Apache License. See [LICENSE](LICENSE) for terms.

[comment]: # (End Copyright)