;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns io.biotz.message-schema.core-test
  (:require [cljc.java-time.instant :as jt.instant]
            [clojure.test :refer :all]
            [io.biotz.message-schema.core :as core]))

(def example-1
  {:message-data
   {"timestamp" "1677161696842" "hum" 10.0 "temp" 20.0 "on" true}
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/object
     ["timestamp"
      [:biotz.message-schema/unix-timestamp-str
       {:record-timestamp true
        :record-name "timestamp"}]]
     ["hum"
      [:biotz.message-schema/decimal
       {:record-name "humidity"}]]
     ["temp"
      [:biotz.message-schema/decimal
       {:record-name "temperature"}]]
     ["on"
      [:biotz.message-schema/nilable
       [:biotz.message-schema/boolean
        {:optional true
         :record-name "on"}]]]]]
   :data
   [{"timestamp" (jt.instant/of-epoch-milli 1677161696842)
     "humidity" 10.0
     "temperature" 20.0
     "on" true}]
   :data-model-metadata
   [{:name "timestamp" :type :unix-timestamp-str}
    {:name "humidity" :type :decimal}
    {:name "temperature" :type :decimal}
    {:name "on" :type :boolean}]})

(def example-2
  {:message-data
   {"firmware-version" "1.0"
    "data" [{"timestamp" 1677161696843 "humidity" 5.0 "temperature" 10.0}
            {"timestamp" 1677161696844 "humidity" 5.0 "temperature" 10.0}]}
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/object
     ["firmware-version"
      [:biotz.message-schema/text
       {:record-name "firmware_version"}]]
     ["data"
      [:biotz.message-schema/coll-of-identical-items
       [:biotz.message-schema/object
        ["timestamp"
         [:biotz.message-schema/unix-timestamp-int
          {:record-timestamp true
           :record-name "timestamp"}]]
        ["humidity"
         [:biotz.message-schema/decimal
          {:record-name "humidity"}]]
        ["temperature"
         [:biotz.message-schema/decimal
          {:record-name "temperature"}]]]]]]]
   :data
   [{"timestamp" (jt.instant/of-epoch-milli 1677161696843)
     "firmware_version" "1.0"
     "humidity" 5.0
     "temperature" 10.0}
    {"timestamp" (jt.instant/of-epoch-milli 1677161696844)
     "firmware_version" "1.0"
     "humidity" 5.0
     "temperature" 10.0}]
   :data-model-metadata
   [{:name "firmware_version" :type :text}
    {:name "timestamp" :type :unix-timestamp-int}
    {:name "humidity" :type :decimal}
    {:name "temperature" :type :decimal}]})

(def example-3
  {:message-data
   {"firmware" {"version" "1.0"}
    "temperatures" [{"timestamp" "2023-02-23T15:42:27Z" "temperature" 10.0}
                    {"timestamp" "2023-02-23T15:43:27Z" "temperature" 10.0}]
    "humidity" [{"timestamp" "2023-02-23T15:42:27Z" "humidity" 10.0}
                {"timestamp" "2023-02-23T15:44:27Z" "humidity" 10.0}]}
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/object
     ["firmware"
      [:biotz.message-schema/object
       ["version"
        [:biotz.message-schema/text
         {:record-name "firmware_version"}]]]]
     ["temperatures"
      [:biotz.message-schema/coll-of-identical-items
       [:biotz.message-schema/object
        ["timestamp"
         [:biotz.message-schema/rfc-3339-timestamp
          {:record-timestamp true
           :record-name "timestamp"}]]
        ["temperature"
         [:biotz.message-schema/decimal
          {:record-name "temperature"}]]]]]
     ["humidity"
      [:biotz.message-schema/coll-of-identical-items
       [:biotz.message-schema/object
        ["timestamp"
         [:biotz.message-schema/rfc-3339-timestamp
          {:record-timestamp true
           :record-name "timestamp"}]]
        ["humidity"
         [:biotz.message-schema/decimal
          {:record-name "humidity"}]]]]]]]
   :data
   [{"timestamp" (jt.instant/parse "2023-02-23T15:42:27Z") "temperature" 10.0 "humidity" 10.0 "firmware_version" "1.0"}
    {"timestamp" (jt.instant/parse "2023-02-23T15:43:27Z") "temperature" 10.0 "firmware_version" "1.0"}
    {"timestamp" (jt.instant/parse "2023-02-23T15:44:27Z") "humidity" 10.0 "firmware_version" "1.0"}]
   :data-model-metadata
   [{:name "firmware_version" :type :text}
    {:name "timestamp" :type :rfc-3339-timestamp}
    {:name "temperature" :type :decimal}
    {:name "humidity" :type :decimal}]})

(def example-4
  {:message-data
   {"timestamp" 1677161696842
    "temperature" {"inner" 10.0 "outer" 10.0}
    "humidity" {"inner" 5.0 "outer" 5.0}}
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/object
     ["timestamp"
      [:biotz.message-schema/unix-timestamp-int
       {:record-timestamp true
        :record-name "timestamp"}]]
     ["temperature"
      [:biotz.message-schema/object
       ["inner"
        [:biotz.message-schema/nilable
         [:biotz.message-schema/decimal
          {:record-name "temperature_inner"}]]]
       ["outer"
        [:biotz.message-schema/decimal
         {:record-name "temperature_outer"}]]]]
     ["humidity"
      [:biotz.message-schema/object
       ["inner"
        [:biotz.message-schema/decimal
         {:record-name "humidity_inner"}]]
       ["outer"
        [:biotz.message-schema/decimal
         {:record-name "humidity_outer"}]]]]]]
   :data
   [{"timestamp" (jt.instant/of-epoch-milli 1677161696842)
     "temperature_inner" 10.0
     "temperature_outer" 10.0
     "humidity_inner" 5.0
     "humidity_outer" 5.0}]
   :data-model-metadata
   [{:name "timestamp" :type :unix-timestamp-int}
    {:name "temperature_inner" :type :decimal}
    {:name "temperature_outer" :type :decimal}
    {:name "humidity_inner" :type :decimal}
    {:name "humidity_outer" :type :decimal}]})

(def example-5
  {:message-data
   [1677161696842 10.0 10.0 true]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-unrelated-items
     [:biotz.message-schema/unix-timestamp-int
      {:record-timestamp true
       :record-name "timestamp"}]
     [:biotz.message-schema/decimal
      {:record-name "temperature"}]
     [:biotz.message-schema/decimal
      {:record-name "humidity"}]
     [:biotz.message-schema/boolean
      {:record-name "on"}]]]
   :data
   [{"timestamp" (jt.instant/of-epoch-milli 1677161696842)
     "temperature" 10.0
     "humidity" 10.0
     "on" true}]
   :data-model-metadata
   [{:name "timestamp" :type :unix-timestamp-int}
    {:name "temperature" :type :decimal}
    {:name "humidity" :type :decimal}
    {:name "on" :type :boolean}]})

(def example-6
  {:message-data
   ["2023-02-23T15:43:27Z" {"temp" 10.0 "hum" 10.0}]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-unrelated-items
     [:biotz.message-schema/rfc-3339-timestamp
      {:record-timestamp true
       :record-name "timestamp"}]
     [:biotz.message-schema/object
      ["temp"
       [:biotz.message-schema/decimal
        {:record-name "temperature"}]]
      ["hum"
       [:biotz.message-schema/decimal
        {:record-name "humidity"}]]]]]
   :data
   [{"timestamp" (jt.instant/parse "2023-02-23T15:43:27Z")
     "temperature" 10.0
     "humidity" 10.0}]
   :data-model-metadata
   [{:name "timestamp" :type :rfc-3339-timestamp}
    {:name "temperature" :type :decimal}
    {:name "humidity" :type :decimal}]})

(def example-7
  {:message-data
   [1677161696842 [10.0 10.0] [5.0 5.0]]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-unrelated-items
     [:biotz.message-schema/unix-timestamp-int
      {:record-timestamp true
       :record-name "timestamp"}]
     [:biotz.message-schema/coll-of-unrelated-items
      [:biotz.message-schema/decimal
       {:record-name "temperature_inner"}]
      [:biotz.message-schema/decimal
       {:record-name "temperature_outer"}]]
     [:biotz.message-schema/coll-of-unrelated-items
      [:biotz.message-schema/decimal
       {:record-name "humidity_inner"}]
      [:biotz.message-schema/decimal
       {:record-name "humidity_outer"}]]]]
   :data
   [{"timestamp" (jt.instant/of-epoch-milli 1677161696842)
     "temperature_inner" 10.0
     "temperature_outer" 10.0
     "humidity_inner" 5.0
     "humidity_outer" 5.0}]
   :data-model-metadata
   [{:name "timestamp" :type :unix-timestamp-int}
    {:name "temperature_inner" :type :decimal}
    {:name "temperature_outer" :type :decimal}
    {:name "humidity_inner" :type :decimal}
    {:name "humidity_outer" :type :decimal}]})

(def example-8
  {:message-data
   [{"timestamp" 1677161696842
     "humidity" 19.0
     "temperature" 5.0}
    {"timestamp" 1677161696843
     "humidity" 20.0
     "temperature" 10.0}]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-identical-items
     [:biotz.message-schema/object
      ["timestamp"
       [:biotz.message-schema/unix-timestamp-int
        {:record-timestamp true
         :record-name "timestamp"}]]
      ["humidity"
       [:biotz.message-schema/decimal
        {:record-name "humidity"}]]
      ["temperature"
       [:biotz.message-schema/decimal
        {:record-name "temperature"}]]]]]
   :data
   [{"timestamp" (jt.instant/of-epoch-milli 1677161696842)
     "humidity" 19.0
     "temperature" 5.0}
    {"timestamp" (jt.instant/of-epoch-milli 1677161696843)
     "humidity" 20.0
     "temperature" 10.0}]
   :data-model-metadata
   [{:name "timestamp" :type :unix-timestamp-int}
    {:name "humidity" :type :decimal}
    {:name "temperature" :type :decimal}]})

(def example-9
  {:message-data
   [[1677161696842 5.0 10.0] [1677161696843 5.0 15.0]]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-identical-items
     [:biotz.message-schema/coll-of-unrelated-items
      [:biotz.message-schema/unix-timestamp-int
       {:record-timestamp true
        :record-name "timestamp"}]
      [:biotz.message-schema/decimal
       {:record-name "temperature"}]
      [:biotz.message-schema/decimal
       {:record-name "humidity"}]]]]
   :data
   [{"timestamp" (jt.instant/of-epoch-milli 1677161696842)
     "temperature" 5.0
     "humidity" 10.0}
    {"timestamp" (jt.instant/of-epoch-milli 1677161696843)
     "temperature" 5.0
     "humidity" 15.0}]
   :data-model-metadata
   [{:name "timestamp" :type :unix-timestamp-int}
    {:name "temperature" :type :decimal}
    {:name "humidity" :type :decimal}]})

(def example-10
  {:message-data 10.5
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/decimal
     {:record-name "temperature"}]]
   :data
   [{"temperature" 10.5}]
   :data-model-metadata
   [{:name "temperature" :type :decimal}]})

(deftest test-custom-malli-schema-registry
  (are [m] (not (core/explain-message-data
                 (:malli-schema m)
                 (:message-data m)))
    example-1 example-2
    example-3 example-4
    example-5 example-6
    example-7 example-8
    example-9 example-10))

(deftest test-malli-meta-schema
  (are [m] (not (core/explain-message-schema
                 (:malli-schema m)))
    example-1 example-2
    example-3 example-4
    example-5 example-6
    example-7 example-8
    example-9 example-10))

(deftest test-data-transformation
  (are [m] (= (:data m)
              ((core/build-message-data-transformer
                (:malli-schema m))
               (:message-data m)))
    example-1 example-2
    ;; example-3 TODO decide how we want to support this
    example-4
    example-5 example-6
    example-7 example-8
    example-9 example-10))

(deftest test-data-model-metadata
  (are [m] (= (:data-model-metadata m)
              (core/schema->data-model-metadata (:malli-schema m)))
    example-1 example-2
    example-3 example-4
    example-5 example-6
    example-7 example-8
    example-9 example-10))

(deftest calculate-new-data-model-metadata-test
  (let [old-schema [:biotz.message-schema/message
                    [:biotz.message-schema/object
                     ["timestamp"
                      [:biotz.message-schema/unix-timestamp-int
                       {:record-timestamp true
                        :record-name "timestamp"}]]
                     ["temp"
                      [:biotz.message-schema/decimal
                       {:record-name "temperature"}]]]]
        old-data-model-metadata (core/schema->data-model-metadata old-schema)]
    (testing "Calculates new changes successfully"
      (let [new-schema [:biotz.message-schema/message
                        [:biotz.message-schema/object
                         ["timestamp"
                          [:biotz.message-schema/unix-timestamp-int
                           {:record-timestamp true
                            :record-name "timestamp"}]]
                         ["temp"
                          [:biotz.message-schema/decimal
                           {:record-name "temperature"}]]
                         ["hum"
                          [:biotz.message-schema/decimal
                           {:record-name "humidity"}]]]]
            new-data-model-metadata (core/schema->data-model-metadata new-schema)
            result (core/calculate-new-data-model-metadata old-data-model-metadata
                                                           new-data-model-metadata)]
        (is (contains? result :to-add))
        (is (contains? result :new-data-model-metadata))
        (is (= 1 (count (:to-add result))))))
    (testing "Type change of existing column meta is forbidden"
      (let [new-schema [:biotz.message-schema/message
                        [:biotz.message-schema/object
                         ["timestamp"
                          [:biotz.message-schema/unix-timestamp-int
                           {:record-timestamp true
                            :record-name "timestamp"}]]
                         ["temp"
                          [:biotz.message-schema/integer
                           {:record-name "temperature"}]]]]
            new-data-model-metadata (core/schema->data-model-metadata new-schema)
            result (try
                     (core/calculate-new-data-model-metadata old-data-model-metadata
                                                             new-data-model-metadata)
                     (catch Throwable t
                       {:message (ex-message t)
                        :data (ex-data t)}))]
        (is (= "New column metadata entry breaks existing column metadata."
               (:message result)))
        (is (= :type-change-is-forbidden (get-in result [:data :error])))))
    (testing "Schema field name change results in adding a new column in data model metadata."
      (let [new-schema [:biotz.message-schema/message
                        [:biotz.message-schema/object
                         ["timestamp"
                          [:biotz.message-schema/unix-timestamp-int
                           {:record-timestamp true
                            :record-name "timestamp"}]]
                         ["temp2"
                          [:biotz.message-schema/integer
                           {:record-name "temperature2"}]]]]
            new-data-model-metadata (core/schema->data-model-metadata new-schema)
            result (core/calculate-new-data-model-metadata old-data-model-metadata
                                                           new-data-model-metadata)]
        (is (contains? result :to-add))
        (is (contains? result :new-data-model-metadata))
        (is (= 1 (count (:to-add result))))
        (is (= "temperature2" (-> result :to-add first :name)))
        (is (= :integer (-> result :to-add first :type)))))
    (testing "Passing the same data model metadata gives the same result."
      (let [new-data-model-metadata (core/schema->data-model-metadata old-schema)
            result (core/calculate-new-data-model-metadata old-data-model-metadata
                                                           new-data-model-metadata)]
        (is (not (contains? result :to-add)))
        (is (contains? result :new-data-model-metadata))))))
