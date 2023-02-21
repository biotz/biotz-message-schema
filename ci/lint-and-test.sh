#!/usr/bin/env bash
#shellcheck disable=SC1010

set -eu -o pipefail

docker run --rm --detach \
    --name "biotz-message-schema" \
    --volume "${PWD}:/app" \
    --entrypoint "/bin/bash" \
    "${IMAGE_TAG}" \
    tail -f /dev/null

echo "clj-kondo"
docker exec biotz-message-schema clj-kondo --lint src --lint test --lint dev

echo "cljfmt"
docker exec biotz-message-schema lein cljfmt fix

echo "eastwood"
docker exec biotz-message-schema lein eastwood

echo "tests"
docker exec biotz-message-schema lein test :all
