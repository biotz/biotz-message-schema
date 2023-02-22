#!/usr/bin/env bash
#shellcheck disable=SC1010

set -e -o pipefail

docker exec biotz-message-schema lein deploy
