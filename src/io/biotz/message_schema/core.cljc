;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns io.biotz.message-schema.core
  (:require [cljc.java-time.instant :as jt.instant]
            [clojure.edn :as edn]
            [io.biotz.message-schema.transformer :as transformer]
            [malli.core :as m]
            [malli.transform :as mt]
            [malli.util :as mu]))

(def ^:private rfc-3339-regex
  #"\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[1-2][0-9]|3[01])T(?:[01][0-9]|2[0-3]):[0-5][0-9]:(?:[0-5][0-9]|60)(?:\.\d{1,9})?Z")

(def ^:private max-long-digits-regex
  #"\d{1,20}")

(defn -unix-timestamp-int-schema
  []
  (m/-simple-schema
   {:type :unix-timestamp-int
    :type-properties {:biotz-type :unix-timestamp-int}
    :pred int?}))

(defn -unix-timestamp-str-schema
  []
  (m/-simple-schema
   {:type :unix-timestamp-str
    :type-properties {:biotz-type :unix-timestamp-str}
    :pred #(and (string? %)
                (re-matches max-long-digits-regex %)
                (int? (edn/read-string %)))}))

(defn -rfc-3339-timestamp-schema
  []
  (m/-simple-schema
   {:type :rfc-3339-timestamp
    :type-properties {:biotz-type :rfc-3339-timestamp}
    :pred #(and (string? %)
                (re-matches rfc-3339-regex %)
                (try
                  (jt.instant/parse %)
                  (catch #?(:clj Throwable :cljs :default) _ nil)))}))

(defn -decimal-schema
  []
  (m/-simple-schema
   {:type :decimal
    :type-properties {:biotz-type :decimal}
    :pred double?}))

(defn -integer-schema
  []
  (m/-simple-schema
   {:type :integer
    :type-properties {:biotz-type :integer}
    :pred int?}))

(defn -text-schema
  []
  (m/-simple-schema
   {:type :text
    :type-properties {:biotz-type :text}
    :pred string?}))

(defn -boolean-schema
  []
  (m/-simple-schema
   {:type :boolean
    :type-properties {:biotz-type :boolean}
    :pred boolean?}))

(def registry
  {;; Basic types
   :biotz.message-schema/decimal (-decimal-schema)
   :biotz.message-schema/integer (-integer-schema)
   :biotz.message-schema/text (-text-schema)
   :biotz.message-schema/unix-timestamp-int (-unix-timestamp-int-schema)
   :biotz.message-schema/unix-timestamp-str (-unix-timestamp-str-schema)
   :biotz.message-schema/rfc-3339-timestamp (-rfc-3339-timestamp-schema)
   :biotz.message-schema/boolean (-boolean-schema)
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

(defn has-timestamp-record?
  "Returns `true` if the given `schema` contains a `record-timestamp`
  set to `true`."
  [schema]
  (mu/find-first schema
                 (fn [s _ _]
                   (-> s m/properties :record-timestamp))
                 {:registry registry}))

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
