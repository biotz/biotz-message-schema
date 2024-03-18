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

(def record-name-regex
  #"^[a-z_][a-z0-9_]{0,63}$")

(def record-name-schema
  (m/schema
   [:and
    [:string {:min 1 :max 64}]
    [:re record-name-regex]]))

(def record-timestamp-name
  "timestamp")

(def object-key-name-schema
  (m/schema
   [:string {:min 1}]))

(defn -unix-timestamp-int-schema
  []
  (m/-simple-schema
   {:type :unix-timestamp-int
    :type-properties {:biotz-type :unix-timestamp-int
                      :biotz-metadata-type :timestamp
                      :decode/biotz-payload-transformer transformer/unix-timestamp-int-decoder}
    :pred int?}))

(defn -unix-timestamp-str-schema
  []
  (m/-simple-schema
   {:type :unix-timestamp-str
    :type-properties {:biotz-type :unix-timestamp-str
                      :biotz-metadata-type :timestamp
                      :decode/biotz-payload-transformer transformer/unix-timestamp-str-decoder}
    :pred #(and (string? %)
                (re-matches max-long-digits-regex %)
                (int? (edn/read-string %)))}))

(defn -rfc-3339-timestamp-schema
  []
  (m/-simple-schema
   {:type :rfc-3339-timestamp
    :type-properties {:biotz-type :rfc-3339-timestamp
                      :biotz-metadata-type :timestamp
                      :decode/biotz-payload-transformer transformer/rfc-3339-timestamp-decoder}
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
    :pred #(or (double? %) (int? %))}))

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

(defn -boolean-as-string-schema
  []
  (m/-simple-schema
   {:type :boolean
    :type-properties
    {:biotz-type :boolean-as-string
     :biotz-metadata-type :boolean
     :decode/biotz-payload-transformer transformer/boolean-as-string-decoder}
    :pred #(and (string? %)
                (re-matches #"^(?i)(true|false)$" %))}))

(defn -boolean-as-integer-schema
  []
  (m/-simple-schema
   {:type :boolean
    :type-properties
    {:biotz-type :boolean-as-integer
     :biotz-metadata-type :boolean
     :decode/biotz-payload-transformer transformer/boolean-as-integer-decoder}
    :pred int?}))

(defn -base10-integer-as-string
  []
  (m/-simple-schema
   {:type :integer
    :type-properties
    {:biotz-type :base10-integer-as-string
     :biotz-metadata-type :integer
     :decode/biotz-payload-transformer transformer/base10-integer-as-string-decoder}
    :pred #(and (string? %)
                (re-matches #"[+-]?[0-9]+" %))}))

(defn -base10-decimal-as-string
  []
  (m/-simple-schema
   {:type :decimal
    :type-properties
    {:biotz-type :base10-decimal-as-string
     :biotz-metadata-type :decimal
     :decode/biotz-payload-transformer transformer/base10-decimal-as-string-decoder}
    :pred #(and (string? %)
                (re-matches #"[+-]?([0-9]*\.[0-9]+|[0-9]+)" %))}))

(defn -base16-integer-as-string
  []
  (m/-simple-schema
   {:type :integer
    :type-properties
    {:biotz-type :base16-integer-as-string
     :biotz-metadata-type :integer
     :decode/biotz-payload-transformer transformer/base16-integer-as-string-decoder}
    :pred #(and (string? %)
                (re-matches #"(?:0x)?[0-9a-fA-F]+" %))}))

(defn- distinct-object-key-names?
  [object-schema]
  (let [key-names (->> (rest object-schema)
                       (map first))]
    (apply distinct? key-names)))

(defn- distinct-record-names?
  [schema]
  (let [record-names (->>
                      (tree-seq vector? rest schema)
                      (filter #(and
                                (map? %)
                                ;; NOTE all record-timestamps have the
                                ;; same fixed name, and they can be
                                ;; repeated.
                                (not (:record-timestamp %))))
                      (keep :record-name))]
    (apply distinct? record-names)))

(defn- has-single-record-timestamp?
  [schema]
  (let [property-maps (->>
                       (tree-seq vector? rest schema)
                       (filter map?))
        record-timestamp-count (->> property-maps
                                    (filter :record-timestamp)
                                    (count))]
    (= 1 record-timestamp-count)))

(defn- has-correct-amount-of-record-timestamps?
  [schema]
  (let [nodes (tree-seq vector? rest schema)
        record-timestamp-count (->> nodes
                                    (filter #(and (map? %) (:record-timestamp %)))
                                    (count))
        coll-of-identical-items-count (->> nodes
                                           (filter #(and (vector? %)
                                                         (= :biotz.message-schema/coll-of-identical-items
                                                            (first %))))
                                           (count))]
    (if (> coll-of-identical-items-count 0)
      (= coll-of-identical-items-count record-timestamp-count)
      (<= record-timestamp-count 1))))

(def registry
  {;; Basic types
   :biotz.message-schema/decimal (-decimal-schema)
   :biotz.message-schema/integer (-integer-schema)
   :biotz.message-schema/text (-text-schema)
   :biotz.message-schema/unix-timestamp-int (-unix-timestamp-int-schema)
   :biotz.message-schema/unix-timestamp-str (-unix-timestamp-str-schema)
   :biotz.message-schema/rfc-3339-timestamp (-rfc-3339-timestamp-schema)
   :biotz.message-schema/boolean (-boolean-schema)
   :biotz.message-schema/boolean-as-string (-boolean-as-string-schema)
   :biotz.message-schema/boolean-as-integer (-boolean-as-integer-schema)
   :biotz.message-schema/base10-integer-as-string (-base10-integer-as-string)
   :biotz.message-schema/base10-decimal-as-string (-base10-decimal-as-string)
   :biotz.message-schema/base16-integer-as-string (-base16-integer-as-string)
   ;; Collections
   :biotz.message-schema/object (m/-map-schema
                                 {:type-properties
                                  {:decode/biotz-payload-transformer transformer/object-decoder}})
   :biotz.message-schema/coll-of-identical-items (m/-collection-schema
                                                  {:type :vector
                                                   :pred vector?
                                                   :empty []})
   :biotz.message-schema/coll-of-unrelated-items (m/-tuple-schema
                                                  {:type-properties
                                                   {:decode/biotz-payload-transformer transformer/coll-of-unrelated-items-decoder}})
   ;; Other
   :biotz.message-schema/nilable (m/-maybe-schema)
   :biotz.message-schema/message (m/-collection-schema
                                  {:type :set
                                   :pred set?
                                   :empty #{}
                                   :in (fn [_ x] x)
                                   :type-properties {:decode/biotz-payload-transformer transformer/message-decoder}})})

(def action-msg-type-schema-meta-schema
  (m/schema
   [:schema
    {:registry {::nilable-field [:multi {:dispatch first}
                                 [:biotz.message-schema/nilable
                                  [:tuple
                                   any?
                                   [:and vector? [:ref ::schema]]]]
                                 [::m/default
                                  [:ref ::schema]]]
                ::object-field [:cat
                                object-key-name-schema
                                [:? [:map [:optional {:optional true} boolean?]]]
                                [:+ [:and vector? [:ref ::nilable-field]]]]
                ::schema [:multi {:dispatch first}
                          [:biotz.message-schema/object
                           [:and
                            [:cat
                             any?
                             [:+ [:and vector? [:ref ::object-field]]]]
                            [:fn distinct-object-key-names?]]]
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
                             (apply conj [:enum] (keys registry))]]]]}}

    [:tuple
     [:= :biotz.message-schema/message]
     ::schema]]))

(def msg-type-schema-meta-schema
  (m/schema
   [:schema
    {:registry {::record-timestamp-properties [:map
                                               {:closed true}
                                               [:record-name
                                                [:= record-timestamp-name]]
                                               [:record-timestamp
                                                [:= true]]]
                ::other-properties [:map
                                    {:closed true}
                                    [:record-name
                                     [:and
                                      record-name-schema
                                      [:not= record-timestamp-name]]]
                                    [:record-timestamp
                                     {:optional true}
                                     [:enum false nil]]]
                ::properties [:or
                              [:ref ::other-properties]
                              [:ref ::record-timestamp-properties]]
                ::nilable-field [:multi {:dispatch first}
                                 [:biotz.message-schema/nilable
                                  [:tuple
                                   any?
                                   [:and vector? [:ref ::schema]]]]
                                 [::m/default
                                  [:ref ::schema]]]
                ::object-field [:cat
                                object-key-name-schema
                                [:? [:map [:optional {:optional true} boolean?]]]
                                [:+ [:and vector? [:ref ::nilable-field]]]]
                ::schema [:multi {:dispatch first}
                          [:biotz.message-schema/object
                           [:and
                            [:cat
                             any?
                             [:+ [:and vector? [:ref ::object-field]]]]
                            [:fn distinct-object-key-names?]]]
                          [:biotz.message-schema/coll-of-identical-items
                           [:and
                            [:tuple
                             any?
                             [:and vector? [:ref ::schema]]]
                            [:fn has-single-record-timestamp?]]]
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

    [:and
     [:tuple
      [:= :biotz.message-schema/message]
      ::schema]
     [:fn distinct-record-names?]
     [:fn has-correct-amount-of-record-timestamps?]]]))

(defn- message-data-schema->validation-schema
  [schema]
  (-> schema
      (m/schema {:registry registry})
      (mu/closed-schema)))

(defn validate-message-data
  [schema message-data]
  (-> schema
      (message-data-schema->validation-schema)
      (m/validate #{message-data})))

(defn explain-message-data
  [schema message-data]
  (-> schema
      (message-data-schema->validation-schema)
      (m/explain #{message-data})))

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
          {:name :biotz-payload-transformer}))]
    (fn [message-data]
      (decoder #{message-data}))))

(defn schema->data-model-metadata
  [schema]
  (reduce
   (fn [data-model-metadata {:keys [schema]}]
     (let [properties (m/properties schema)
           type-properties (m/type-properties schema)]
       (cond-> data-model-metadata
         (and (seq properties)
              (not (some #(= (:record-name properties) (:name %)) data-model-metadata)))
         (conj {:name (:record-name properties)
                :type (or (get type-properties :biotz-metadata-type)
                          (get type-properties :biotz-type))}))))
   []
   (mu/subschemas schema {:registry registry})))

(defn schemas->data-model-metadata
  [schemas]
  (let [xf (comp
            (mapcat schema->data-model-metadata)
            (distinct))]
    (into [] xf schemas)))

(defn calculate-new-data-model-metadata
  [old-data-model-metadata new-data-model-metadata]
  (reduce
   (fn [acc {:keys [name type] :as new-col-metadata}]
     (let [old-col-metadata (some #(when (= name (:name %)) %) old-data-model-metadata)]
       (cond
         (and old-col-metadata
              (not= type (:type old-col-metadata)))
         (throw
          (ex-info "New column metadata entry breaks existing column metadata."
                   {:new-col-metadata new-col-metadata
                    :old-col-metadata old-col-metadata
                    :error :type-change-is-forbidden}))
         (not old-col-metadata)
         (-> acc
             (update :to-add conj new-col-metadata)
             (update :new-data-model-metadata conj new-col-metadata))

         :else acc)))
   {:new-data-model-metadata old-data-model-metadata}
   new-data-model-metadata))
