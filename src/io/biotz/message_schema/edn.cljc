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
