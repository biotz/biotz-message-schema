;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns io.biotz.message-schema.core
  (:require [clojure.edn :as edn]
            [io.biotz.message-schema.transformer :as transformer]
            [malli.core :as m]
            [malli.transform :as mt]))

(def ^:private rfc-3339-regex
  #"((?:(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2}(?:\.\d+)?)))Z")

(def ^:private max-long-digits-regex
  #"\d{1,20}")

(defn -unix-timestamp-int-schema
  []
  (m/-simple-schema
   {:type :unix-timestamp-int
    :pred int?}))

(defn -unix-timestamp-str-schema
  []
  (m/-simple-schema
   {:type :unix-timestamp-str
    :pred #(and (string? %)
                (re-matches max-long-digits-regex %)
                (int? (edn/read-string %)))}))

(defn -rfc-3339-timestamp-schema
  []
  (m/-simple-schema
   {:type :rfc-3339-timestamp
    :pred #(and (string? %)
                (re-matches rfc-3339-regex %))}))

(def registry
  {;; Basic types
   :biotz.message-schema/decimal (m/-double-schema)
   :biotz.message-schema/integer (m/-int-schema)
   :biotz.message-schema/text (m/-string-schema)
   :biotz.message-schema/unix-timestamp-int (-unix-timestamp-int-schema)
   :biotz.message-schema/unix-timestamp-str (-unix-timestamp-str-schema)
   :biotz.message-schema/rfc-3339-timestamp (-rfc-3339-timestamp-schema)
   :biotz.message-schema/boolean (m/-boolean-schema)
   ;; Collections
   :biotz.message-schema/object (m/-map-schema)
   :biotz.message-schema/coll-of-identical-items (m/-collection-schema
                                                  {:type :vector
                                                   :pred vector?
                                                   :empty []})
   :biotz.message-schema/coll-of-unrelated-items (m/-tuple-schema)
   ;; Other
   :biotz.message-schema/nilable (m/-maybe-schema)
   :biotz.message-schema/message (m/-collection-schema
                                  {:type :set
                                   :pred set?
                                   :empty #{}
                                   :in (fn [_ x] x)})})

(def meta-schema
  (m/schema
   [:schema
    {:registry {::properties [:map
                              [:optional
                               {:optional true}
                               boolean?]
                              [:record-name
                               [:and
                                [:string {:min 1, :max 64}]
                                [:re #"^[a-z_][a-z0-9_]+$"]]]
                              [:record-timestamp
                               {:optional true}
                               boolean?]]
                ::nilable-field [:multi {:dispatch first}
                                 [:biotz.message-schema/nilable
                                  [:tuple
                                   any?
                                   [:and vector? [:ref ::schema]]]]
                                 [::m/default
                                  [:ref ::schema]]]
                ::object-field [:cat
                                [:string {:min 1}]
                                [:+ [:and vector? [:ref ::nilable-field]]]]
                ::schema [:multi {:dispatch first}
                          [:biotz.message-schema/object
                           [:cat
                            any?
                            [:+ [:and vector? [:ref ::object-field]]]]]
                          [:biotz.message-schema/coll-of-identical-items
                           [:tuple
                            any?
                            [:and vector? [:ref ::schema]]]]
                          [:biotz.message-schema/coll-of-unrelated-items
                           [:cat
                            any?
                            [:+ [:and vector? [:ref ::nilable-field]]]]]
                          [::m/default
                           [:tuple
                            [:and
                             qualified-keyword?
                             (apply conj [:enum] (keys registry))]
                            [:ref ::properties]]]]}}

    [:tuple
     [:= :biotz.message-schema/message]
     ::schema]]))

(defn validate-message-schema
  [schema]
  (m/validate
   meta-schema
   schema
   {:registry registry}))

(defn explain-message-schema
  [schema]
  (m/explain
   meta-schema
   schema
   {:registry registry}))

(defn validate-message-data
  [schema message-data]
  (m/validate
   schema
   #{message-data}
   {:registry registry}))

(defn explain-message-data
  [schema message-data]
  (m/explain
   schema
   #{message-data}
   {:registry registry}))

(defn build-message-data-validator
  [schema]
  (let [validator
        (m/validator
         schema
         {:registry registry})]
    (fn [message-data]
      (validator #{message-data}))))

(defn build-message-data-transformer
  [schema]
  (let [decoder
        (m/decoder
         schema
         {:registry registry}
         (mt/transformer
          {:name :biotz-payload-transformer
           ;; FIXME: once Malli releases a new version with our PR
           ;; applied, please change the decoders to use our own
           ;; types.
           :decoders {:map {:compile transformer/object-transformer}
                      :tuple {:compile transformer/coll-of-unrelated-items-transformer}
                      :set {:compile transformer/message-transformer}
                      :unix-timestamp-int {:compile transformer/unix-timestamp-int-transformer}
                      :unix-timestamp-str {:compile transformer/unix-timestamp-str-transformer}
                      :rfc-3339-timestamp {:compile transformer/rfc-3339-timestamp-transformer}}}))]
    (fn [message-data]
      (decoder #{message-data}))))
