(ns xapi-schema.core-spec
  #+cljs (:require-macros [speclj.core :refer [describe it should should= should-not run-specs]])
  #+cljs (:require [speclj.core]
                   [xapi-schema.core :refer [foo]])
  #+clj (:require [speclj.core :refer :all]
                  [xapi-schema.core :refer :all]))

(describe "foo"
          (it "returns bar"
              (should= (foo) "bar")))
