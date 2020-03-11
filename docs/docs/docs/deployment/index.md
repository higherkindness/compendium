---
layout: docs
title: Deployment
url: /docs/deployment
---

# Deployment

## Requirements

In order to deploy Compendium, it is necessary to provide an storage for both metadata and protocols. **Each one will use its own database** to store the necessary information.

#### Supported storages for metadata:

- PostgreSQL

#### Supported storages for protocols:

- PostgreSQL

## Compendium configuration variables

Compendium service offers multiple variables that allows user to configure how the service is deployed. All variables are defined at Compendium's [application.conf][applicationconf].

#### Metadata Storage configuration:

- `COMPENDIUM_METADATA_STORAGE_JDBC_URL`
- `COMPENDIUM_METADATA_STORAGE_USERNAME`
- `COMPENDIUM_METADATA_STORAGE_PASSWORD`
- `COMPENDIUM_METADATA_STORAGE_CONNECTION_TIMEOUT`
- `COMPENDIUM_METADATA_STORAGE_IDLE_TIMEOUT`
- `COMPENDIUM_METADATA_STORAGE_MAX_LIFETIME`
- `COMPENDIUM_METADATA_STORAGE_MINIMUM_IDLE`
- `COMPENDIUM_METADATA_STORAGE_MAXIMUM_POOL_SIZE`

#### Protocol Storage configuration:

- `COMPENDIUM_PROTOCOLS_STORAGE_JDBC_URL`
- `COMPENDIUM_PROTOCOLS_STORAGE_USERNAME`
- `COMPENDIUM_PROTOCOLS_STORAGE_PASSWORD`
- `COMPENDIUM_PROTOCOLS_STORAGE_CONNECTION_TIMEOUT`
- `COMPENDIUM_PROTOCOLS_STORAGE_IDLE_TIMEOUT`
- `COMPENDIUM_PROTOCOLS_STORAGE_MAX_LIFETIME`
- `COMPENDIUM_PROTOCOLS_STORAGE_MINIMUM_IDLE`
- `COMPENDIUM_PROTOCOLS_STORAGE_MAXIMUM_POOL_SIZE`

[applicationconf]: https://github.com/higherkindness/compendium/blob/master/src/main/resources/application.conf
