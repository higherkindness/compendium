---
layout: docs
title: Docker deployment
url: /docs/deployment/docker
---

# Deployment using Docker

Compendium docker image could be found here: [47deg/compendium][docker-hub]

## Running using `docker`

To run Compendium using `docker`:

``` shell
docker run -p 8080:8080 \
  -e "COMPENDIUM_METADATA_STORAGE_JDBC_URL=<metadata-storage-jdbc-url>" \
  -e "COMPENDIUM_METADATA_STORAGE_USERNAME=<metadata-storage-username>" \
  -e "COMPENDIUM_METADATA_STORAGE_PASSWORD=<metadata-storage-password>" \
  -e "COMPENDIUM_PROTOCOLS_STORAGE_JDBC_URL=<protocols-storage-jdbc-url>" \
  -e "COMPENDIUM_PROTOCOLS_STORAGE_USERNAME=<protocols-storage-username>" \
  -e "COMPENDIUM_PROTOCOLS_STORAGE_PASSWORD=<protocols-storage-password>" \
  47deg/compendium:latest
```

## Running using `docker-compose`

To make easier to run Compendium, a [`docker-compose.yaml`][docker-compose] file is available. It will set up:

- PostgreSQL Database with two database:
  - `compendium_metadata`
  - `compendium_protocols`
- Compendium instance connected to PostgreSQL

The files required for using  are:
- [`docker-compose.yaml`][docker-compose]
- [`.docker-compose/`][.docker-compose]: contains all necessary files used by docker-compose file.
  - `init.db`: contains all necessary scripts to set up PostgreSQL database:
    - `init-compendium-metadata-db.sh`: it will create the database for metadata
    - `init-compendium-protocols-db.sh`: it will create the database for protocols

To run Compendium using `docker-compose`:

``` shell
docker-compose up
```

[docker-hub]: https://hub.docker.com/r/47deg/compendium
[docker-compose]: https://github.com/higherkindness/compendium/blob/master/docker-compose.yaml
[.docker-compose]: https://github.com/higherkindness/compendium/blob/master/.docker-compose
