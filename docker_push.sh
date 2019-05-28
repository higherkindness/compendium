#!/bin/bash
echo "$1" | docker login -u "$2" --password-stdin
export tag=$(cut -d'=' -f2  version.sbt | sed -e 's/^[ \t]*//;s/"//g')
docker push $2/compendium:${tag}
docker push $2/compendium:latest
