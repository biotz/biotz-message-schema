(ns io.biotz.message-schema.transformer-test
  (:require [clojure.test :refer :all]
            [io.biotz.message-schema.transformer :as transformer]
            [malli.core :as m]
            [malli.transform :as mt]))

(deftest boolean-as-string-decoder-test
  (let [schema [:boolean
                {:decode/test-transformer transformer/boolean-as-string-decoder}]
        transformer (mt/transformer {:name :test-transformer})
        decoder  (m/decoder schema transformer)]
    (testing "Testing boolean-as-string-decoder true values"
      (are [x] (true? (decoder x))
        "true" "True" "TrUe" "TRUE"))
    (testing "Testing boolean-as-string-decoder false values"
      (are [x] (false? (decoder x))
        "false" "False" "FalSe" "FALSE"))))

(deftest boolean-as-integer-decoder-test
  (let [schema [:boolean
                {:decode/test-transformer transformer/boolean-as-integer-decoder}]
        transformer (mt/transformer {:name :test-transformer})
        decoder  (m/decoder schema transformer)]
    (testing "Testing boolean-as-integer-decoder"
      (is (true? (decoder 1)))
      (is (true? (decoder -1)))
      (is (false? (decoder 0))))))

(deftest scale+offset-value-transformer-test
  (testing "Testing scale+offset-value-transformar"
    (letfn [(transform-value [value transformations]
              (let [schema [:any
                            {:transformations transformations
                             :decode/test-transformer transformer/value-transformer}]
                    transformer (mt/transformer {:name :test-transformer})]
                (m/decode schema value transformer)))]
      (are [transformations value result]
           (= result (transform-value value transformations))
        [{:type :scale+offset :parameters {:offset 10 :scale 2}}]
        0 10

        [{:type :scale+offset :parameters {:offset 0 :scale 1}}]
        1 1

        [{:type :scale+offset :parameters {:offset 0 :scale 2}}]
        1 2

        [{:type :scale+offset :parameters {:offset -10 :scale 2}}]
        2 -6

        [{:type :scale+offset :parameters {:offset 10.5 :scale 1.5}}]
        2 13.5))))
