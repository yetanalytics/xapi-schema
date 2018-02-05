(ns xapi-schema.spec-test
  (:require [clojure.test :refer [deftest is testing] #?@(:cljs [:include-macros true])]
            [clojure.spec.alpha :as s #?@(:cljs [:include-macros true])]
            [xapi-schema.spec :as xs]
            [xapi-schema.support.data :refer [long-statement]]
            [clojure.test.check.clojure-test :refer [defspec] :include-macros true]
            [xapi-schema.support.gen :refer [spec-gen]]
            [clojure.test.check.properties :as prop :include-macros true]
            [xapi-schema.core :as core]))

(deftest conform-unform-test
  (is (= long-statement (s/unform ::xs/statement (s/conform ::xs/statement long-statement)))))

(def prop-satisfies-old-schema
  (prop/for-all [s (spec-gen ::xs/statement)]
                (nil? (core/statement-checker s))))

(defspec satisfies-old-schema-test
  10
  prop-satisfies-old-schema)
