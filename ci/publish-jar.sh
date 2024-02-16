#!/usr/bin/env bash
#shellcheck disable=SC1010

set -e -o pipefail

docker exec \
    --env "MVN_PRIVATE_REPO_USERNAME" \
    --env "MVN_PRIVATE_REPO_PASSWORD" \
    biotz-message-schema \
    lein deploy private-mvn-repo
