#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin
export tag=$(cut -d'=' -f2  version.sbt | sed -e 's/^[ \t]*//;s/"//g')
docker push $DOCKER_USER/compendium:${tag}
