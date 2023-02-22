#!/usr/bin/env bash
#shellcheck disable=SC1010

set -e -o pipefail

IMAGE_TAG=local/biotz-message-schema/ci:latest

docker build -t "${IMAGE_TAG}" .

docker run --rm --detach \
    --name "biotz-message-schema" \
    --volume "${PWD}:/app" \
    "${IMAGE_TAG}" \
    tail -f /dev/null

docker exec biotz-message-schema lein deploy
