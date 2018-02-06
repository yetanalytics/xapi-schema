(ns xapi-schema.spec-test
  (:require [clojure.test :refer [deftest is testing] #?@(:cljs [:include-macros true])]
            [clojure.spec.alpha :as s :include-macros true]
            [xapi-schema.spec :as xs :include-macros true]
            [xapi-schema.support.data :refer [long-statement]]
            [clojure.test.check.clojure-test :refer [defspec] :include-macros true]
            [clojure.test.check.properties :as prop :include-macros true]
            [xapi-schema.core :as core]))

(deftest conform-unform-test
  (is (= long-statement (s/unform ::xs/statement (s/conform ::xs/statement long-statement)))))

(def prop-satisfies-old-schema
  (prop/for-all [s (s/gen ::xs/statement)]
                (nil? (core/statement-checker s))))

(defspec satisfies-old-schema-test ;; TODO: often breaks in cljs
  100
  prop-satisfies-old-schema)
