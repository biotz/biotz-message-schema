#!/usr/bin/env bash

set -eu -o pipefail

IMAGE_TAG=local/biotz-message-schema/dev:latest

CONTAINER="$(docker ps --all --quiet --filter "name=biotz-message-schema")"

if [[ -n "${CONTAINER}" ]]; then
    docker kill "${CONTAINER}"
fi

docker build -t "${IMAGE_TAG}" .

docker run --rm --detach \
    --name "biotz-message-schema" \
    --publish "127.0.0.1:4001:4001" \
    --volume "${PWD}:/app" \
    --volume "${HOME}/.m2:/root/.m2" \
    --volume "${HOME}/.m2:/home/hop/.m2" \
    "${IMAGE_TAG}" \
    lein repl :headless

docker logs --follow --timestamps biotz-message-schema
