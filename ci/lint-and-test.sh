#!/usr/bin/env bash
#shellcheck disable=SC1010

set -eu -o pipefail

echo "clj-kondo"
docker exec biotz-message-schema clj-kondo --lint src --lint test

echo "cljfmt"
docker exec biotz-message-schema lein cljfmt fix

echo "eastwood"
docker exec biotz-message-schema lein eastwood

echo "tests"
docker exec biotz-message-schema lein test :all
