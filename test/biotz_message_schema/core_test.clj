;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns biotz-message-schema.core-test
  (:require [biotz-message-schema.core :as core]
            [clojure.test :refer :all]))

(def example-1
  {:message-data
   {"timestamp" 1 "hum" 10.0 "temp" 20.0 "on" true}
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/object
     ["timestamp"
      [:biotz.message-schema/timestamp
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
   [{"timestamp" 1
     "humidity" 10.0
     "temperature" 20.0
     "on" true}]})

(def example-2
  {:message-data
   {"firmware-version" "1.0"
    "data" [{"timestamp" 1 "humidity" 5.0 "temperature" 10.0}
            {"timestamp" 2 "humidity" 5.0 "temperature" 10.0}]}
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
         [:biotz.message-schema/timestamp
          {:record-timestamp true
           :record-name "timestamp"}]]
        ["humidity"
         [:biotz.message-schema/decimal
          {:record-name "humidity"}]]
        ["temperature"
         [:biotz.message-schema/decimal
          {:record-name "temperature"}]]]]]]]
   :data
   [{"timestamp" 1
     "firmware_version" "1.0"
     "humidity" 5.0
     "temperature" 10.0}
    {"timestamp" 2
     "firmware_version" "1.0"
     "humidity" 5.0
     "temperature" 10.0}]})

(def example-3
  {:message-data
   {"firmware" {"version" "1.0"}
    "temperatures" [{"timestamp" 1 "temperature" 10.0}
                    {"timestamp" 2 "temperature" 10.0}]
    "humidity" [{"timestamp" 1 "humidity" 10.0}
                {"timestamp" 3 "humidity" 10.0}]}
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
         [:biotz.message-schema/timestamp
          {:record-timestamp true
           :record-name "timestamp"}]]
        ["temperature"
         [:biotz.message-schema/decimal
          {:record-name "temperature"}]]]]]
     ["humidity"
      [:biotz.message-schema/coll-of-identical-items
       [:biotz.message-schema/object
        ["timestamp"
         [:biotz.message-schema/timestamp
          {:record-timestamp true
           :record-name "timestamp"}]]
        ["humidity"
         [:biotz.message-schema/decimal
          {:record-name "humidity"}]]]]]]]
   :data
   [{"timestamp" 1 "temperature" 10.0 "humidity" 10.0 "firmware_version" "1.0"}
    {"timestamp" 2 "temperature" 10.0 "firmware_version" "1.0"}
    {"timestamp" 3 "humidity" 10.0 "firmware_version" "1.0"}]})

(def example-4
  {:message-data
   {"timestamp" 1
    "temperature" {"inner" 10.0 "outer" 10.0}
    "humidity" {"inner" 5.0 "outer" 5.0}}
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/object
     ["timestamp"
      [:biotz.message-schema/timestamp
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
   [{"timestamp" 1
     "temperature_inner" 10.0
     "temperature_outer" 10.0
     "humidity_inner" 5.0
     "humidity_outer" 5.0}]})

(def example-5
  {:message-data
   [1 10.0 10.0 true]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-unrelated-items
     [:biotz.message-schema/timestamp
      {:record-timestamp true
       :record-name "timestamp"}]
     [:biotz.message-schema/decimal
      {:record-name "temperature"}]
     [:biotz.message-schema/decimal
      {:record-name "humidity"}]
     [:biotz.message-schema/boolean
      {:record-name "on"}]]]
   :data
   [{"timestamp" 1
     "temperature" 10.0
     "humidity" 10.0
     "on" true}]})

(def example-6
  {:message-data
   [1 {"temp" 10.0 "hum" 10.0}]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-unrelated-items
     [:biotz.message-schema/timestamp
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
   [{"timestamp" 1
     "temperature" 10.0
     "humidity" 10.0}]})

(def example-7
  {:message-data
   [1 [10.0 10.0] [5.0 5.0]]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-unrelated-items
     [:biotz.message-schema/timestamp
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
   [{"timestamp" 1
     "temperature_inner" 10.0
     "temperature_outer" 10.0
     "humidity_inner" 5.0
     "humidity_outer" 5.0}]})

(def example-8
  {:message-data
   [{"timestamp" 1
     "humidity" 19.0
     "temperature" 5.0}
    {"timestamp" 2
     "humidity" 20.0
     "temperature" 10.0}]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-identical-items
     [:biotz.message-schema/object
      ["timestamp"
       [:biotz.message-schema/timestamp
        {:record-timestamp true
         :record-name "timestamp"}]]
      ["humidity"
       [:biotz.message-schema/decimal
        {:record-name "humidity"}]]
      ["temperature"
       [:biotz.message-schema/decimal
        {:record-name "temperature"}]]]]]
   :data
   [{"timestamp" 1
     "humidity" 19.0
     "temperature" 5.0}
    {"timestamp" 2
     "humidity" 20.0
     "temperature" 10.0}]})

(def example-9
  {:message-data
   [[1 5.0 10.0] [2 5.0 15.0]]
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/coll-of-identical-items
     [:biotz.message-schema/coll-of-unrelated-items
      [:biotz.message-schema/timestamp
       {:record-timestamp true
        :record-name "timestamp"}]
      [:biotz.message-schema/decimal
       {:record-name "temperature"}]
      [:biotz.message-schema/decimal
       {:record-name "humidity"}]]]]
   :data
   [{"timestamp" 1
     "temperature" 5.0
     "humidity" 10.0}
    {"timestamp" 2
     "temperature" 5.0
     "humidity" 15.0}]})

(def example-10
  {:message-data 10.5
   :malli-schema
   [:biotz.message-schema/message
    [:biotz.message-schema/decimal
     {:record-name "temperature"}]]
   :data
   [{"temperature" 10.5}]})

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
