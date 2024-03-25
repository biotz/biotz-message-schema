;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns io.biotz.message-schema.transformer
  (:require #?(:cljs [clojure.string :as str])
            [cljc.java-time.instant :as jt.instant]
            [clojure.set :as set]
            [malli.core :as m]
            [malli.util :as mu]))

(defn- get-children
  [schema]
  (->> (mu/subschemas schema)
       (filter #(= 1 (count (:path %))))
       (map
        (fn [{:keys [schema] :as children}]
          (cond-> children
            (= :maybe (m/type schema))
            (update :schema (comp first m/children)))))))

(defn- find-record-names
  [schemas]
  (map
   (fn [schema]
     (:record-name (m/properties schema)))
   schemas))

(defn- build-object-rename-kmap
  [schema children]
  (let [children-names (mu/keys schema)
        children-record-names (find-record-names (map :schema children))]
    (->> (zipmap children-names children-record-names)
         (remove #(or
                   (nil? (second %))
                   (= (first %) (second %))))
         (into {}))))

(defn- build-object-merge-types
  [children]
  (reduce
   (fn [merge-types {:keys [path schema]}]
     (let [merge-type (cond
                        (mu/find-first schema (fn [s _ _]
                                                (= :vector (m/type s))))
                        :coll-of-identical-item-ks

                        (mu/find-first schema (fn [s _ _]
                                                (get #{:map :tuple} (m/type s))))
                        :object-ks

                        :else
                        :simple-type-ks)
           key (or (:record-name (m/properties schema))
                   (first path))]
       (update merge-types merge-type conj key)))
   {}
   children))

(def object-decoder
  {:compile
   (fn [schema _]
     (let [children (get-children schema)
           rename-kmap (build-object-rename-kmap schema children)
           {:keys [coll-of-identical-item-ks object-ks simple-type-ks]}
           (build-object-merge-types children)]
       {:leave
        (fn [x]
          (let [renamed-map (set/rename-keys x rename-kmap)
                simple-type-ks-map (select-keys renamed-map simple-type-ks)
                object-ks-maps (keep renamed-map object-ks)]
            (if-not (seq coll-of-identical-item-ks)
              (into simple-type-ks-map object-ks-maps)
              (->> (mapcat renamed-map coll-of-identical-item-ks)
                   (mapv #(-> (into simple-type-ks-map %)
                              (into object-ks-maps)))))))}))})

(def coll-of-unrelated-items-decoder
  {:compile
   (fn [schema _]
     (let [children (get-children schema)
           children-record-names (find-record-names (map :schema children))]
       {:leave
        (fn [x]
          (->> x
               (map
                (fn [record-name v]
                  (if record-name
                    {record-name v}
                    v))
                children-record-names)
               (into {})))}))})

(def message-decoder
  {:compile
   (fn [schema _]
     (let [child (first (get-children schema))]
    ;;TODO refactor to perform operations based on schema
       {:leave
        (fn [x]
          (let [x (first x)]
            (cond
              (map? x)
              [x]
              (sequential? x)
              x
              :else
              [{(:record-name (m/properties (:schema child))) x}])))}))})

(def unix-timestamp-int-decoder
  {:enter jt.instant/of-epoch-milli})

(def unix-timestamp-str-decoder
  {:enter
   (fn [x]
     (let [y #?(:clj (Long/parseUnsignedLong x)
                :cljs (js/parseInt x))]
       (jt.instant/of-epoch-milli y)))})

(def rfc-3339-timestamp-decoder
  {:enter jt.instant/parse})

(def base10-integer-as-string-decoder
  {:enter
   (fn [x]
     #?(:clj (Long/parseLong x 10)
        :cljs (js/parseInt x 10)))})

(def base10-decimal-as-string-decoder
  {:enter
   (fn [x]
     #?(:clj (Double/parseDouble x)
        :cljs (js/parseFloat x)))})

(def base16-integer-as-string-decoder
  {:enter
   (fn [x]
     (let [hex (re-matches #"(?:0x)?([0-9a-fA-F]+)" x)]
       #?(:clj (Long/parseLong (second hex) 16)
          :cljs (js/parseInt (second hex) 16))))})

(def boolean-as-string-decoder
  {:enter
   (fn [x]
     #?(:clj (Boolean/parseBoolean x)
        :cljs (boolean (= "true" (str/lower-case x)))))})

(def boolean-as-integer-decoder
  {:enter
   (fn [x]
     (not= 0 x))})

(defmulti transform-value
  (fn [_value transformer]
    (:type transformer)))

(defmethod transform-value :offset+scale
  [value {{:keys [offset scale]} :parameters}]
  (+ (* value scale) offset))

(def value-transformer
  {:compile
   (fn [schema _]
     (let [transformations (:transformations (m/properties schema))]
       (when (seq transformations)
         {:leave
          (fn [x]
            (reduce transform-value x transformations))})))})
