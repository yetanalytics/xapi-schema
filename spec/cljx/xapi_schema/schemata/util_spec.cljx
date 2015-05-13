(ns xapi-schema.schemata.util-spec
  #+cljs (:require-macros [speclj.core :refer [describe context with it should should= should-not run-specs]])
  #+cljs (:require [speclj.core]
                   [xapi-schema.schemata.util :refer [check-type
                                                      object-type-dispatch]]
                   [schema.core :as s
                    :include-macros true])
  #+clj (:require [speclj.core :refer :all]
                  [schema.core :as s]
                  [xapi-schema.schemata.util :refer :all]))

(describe "check-type"
          (with pred (check-type "Activity"))
          (it "returns a predicate that checks for the given objectType"
              (should (@pred {"objectType" "Activity"}))
              (should-not (@pred {"objectType" "Agent"}))))

(describe "object-type-dispatch"
          (with foo-schema
                {(s/required-key "objectType") s/Str
                 (s/required-key "thing") s/Str})
          (with bar-schema
                {(s/required-key "objectType") s/Str
                 (s/required-key "thing") s/Num})
          (with baz-schema {(s/required-key "thing") s/Keyword})


          (with foo {"objectType" "foo"
                     "thing" "whatever"})
          (with bar {"objectType" "bar"
                     "thing" 2})
          (with baz {"thing" :whatever})

          (with conditional-schema (object-type-dispatch
                                    "foo" @foo-schema
                                    "bar" @bar-schema
                                    :else @baz-schema))
          (it "returns a conditional schema for the given map of object types"
              (should-not (or (s/check @conditional-schema
                                       @foo)
                              (s/check @conditional-schema
                                       @bar)
                              (s/check @conditional-schema
                                       @baz)))
              (should (and
                       (s/check @conditional-schema {"objectType" "foo"
                                                     "thing" :whatever})
                       (s/check @conditional-schema {"thing" "foo"})))))
