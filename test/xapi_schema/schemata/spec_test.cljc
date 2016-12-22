(ns xapi-schema.schemata.spec-test
  (:require [clojure.test :refer :all]
            [xapi-schema.schemata.spec :refer :all]
            [cheshire.core :as c]
            [clojure.spec :as s]))

(def long-statement (c/parse-string (slurp "resources/data/statements/long.json")))

(deftest conform-unform-test
  (is (= long-statement
         (->> long-statement
              (s/conform :xapi-schema.schemata.spec/statement)
              (s/unform :xapi-schema.schemata.spec/statement)))))
