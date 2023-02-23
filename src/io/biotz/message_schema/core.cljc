;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns io.biotz.message-schema.core
  (:require [io.biotz.message-schema.transformer :as transformer]
            [malli.core :as m]
            [malli.transform :as mt]))

(def registry
  {;; Basic types
   :biotz.message-schema/decimal (m/-double-schema)
   :biotz.message-schema/integer (m/-int-schema)
   :biotz.message-schema/text (m/-string-schema)
   :biotz.message-schema/timestamp (m/-int-schema)
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
                      :set {:compile transformer/message-transformer}}}))]
    (fn [message-data]
      (decoder #{message-data}))))
