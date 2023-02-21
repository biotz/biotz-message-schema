#!/usr/bin/env bash
#shellcheck disable=SC1010

set -eu -o pipefail

echo "cljfmt"
lein cljfmt fix

echo "eastwood"
lein eastwood

echo "test"
lein test :all
