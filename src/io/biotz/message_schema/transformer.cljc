;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns io.biotz.message-schema.transformer
  (:require [cljc.java-time.instant :as jt.instant]
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
                        :coll-of-identical-items

                        (mu/find-first schema (fn [s _ _]
                                                (get #{:map :tuple} (m/type s))))
                        :object

                        :else
                        :simple-type)]
       (update merge-types merge-type conj (first path))))
   {}
   children))

(defn- map-merge-submaps
  [m ks]
  (reduce
   (fn [m k]
     (-> m
         (dissoc k)
         (merge (get m k))))
   m
   ks))

(defn- map-merge-colls
  [m ks]
  (let [common (apply dissoc m ks)
        colls (apply concat (vals (select-keys m ks)))]
    (mapv (partial merge common) colls)))

(def object-decoder
  {:compile
   (fn [schema _]
     (let [children (get-children schema)
           rename-kmap (build-object-rename-kmap schema children)
           merge-types (build-object-merge-types children)]
       ;;TODO Use transducers
       {:leave
        (fn [x]
          (cond-> x
            (seq rename-kmap)
            (set/rename-keys rename-kmap)

            (:object merge-types)
            (map-merge-submaps (:object merge-types))

            (:coll-of-identical-items merge-types)
            (map-merge-colls (:coll-of-identical-items merge-types))))}))})

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
