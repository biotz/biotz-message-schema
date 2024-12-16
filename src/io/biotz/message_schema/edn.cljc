;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns io.biotz.message-schema.edn
  (:refer-clojure :exclude [read-string])
  (:require [clojure.edn :as edn]))

(defn write-string
  "Given a BIoTZ message-schema, it returns the stringified version of
  it."
  [schema]
  (pr-str schema))

(defn read-string
  "Given a stringified BIoTZ message-schema, it returns the EDN version
  of it."
  [str-schema]
  (edn/read-string str-schema))
