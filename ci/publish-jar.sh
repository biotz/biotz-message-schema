#!/usr/bin/env bash
#shellcheck disable=SC1010

set -eu -o pipefail

# Skip building if no tag
if [[ -z "${BITBUCKET_TAG}" ]]; then
    exit 0
fi

docker exec biotz-message-schema lein deploy
