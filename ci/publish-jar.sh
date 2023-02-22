#!/usr/bin/env bash
#shellcheck disable=SC1010

set -e -o pipefail

IMAGE_TAG=local/biotz-message-schema/ci:latest

docker build -t "${IMAGE_TAG}" .

docker run --rm --detach \
    --env "AWS_ACCESS_KEY_ID" \
    --env "AWS_SECRET_ACCESS_KEY" \
    --env "AWS_DEFAULT_REGION" \
    --name "biotz-message-schema" \
    --volume "${PWD}:/app" \
    "${IMAGE_TAG}" \
    tail -f /dev/null

docker exec biotz-message-schema lein deploy magnet-s3-repo
