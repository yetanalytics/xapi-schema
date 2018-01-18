(ns xapi-schema.spec-test
  (:require [clojure.test :refer [deftest is testing] #?@(:cljs [:include-macros true])]
            [clojure.spec.alpha :as s #?@(:cljs [:include-macros true])]
            [xapi-schema.spec :as xs]
            [xapi-schema.support.data :refer [long-statement]]))

(deftest conform-unform-test
  (is (= long-statement (s/unform ::xs/statement (s/conform ::xs/statement long-statement)))))
