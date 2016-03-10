(ns xapi-schema.schemata.util-spec
  #?@(:cljs [(:require-macros [speclj.core :refer [describe
                                                   context
                                                   with
                                                   it
                                                   should
                                                   should=
                                                   should-not
                                                   run-specs
                                                   should-not-throw]])
             (:require [speclj.core]
                       [xapi-schema.schemata.util :refer [check-type
                                                          object-type-dispatch
                                                          error->vec
                                                          error-val->json
                                                          errors->json
                                                          errors->data
                                                          named-error?
                                                          validation-error?
                                                          leaves-and-paths
                                                          errors->paths]]
                       [xapi-schema.schemata.json :as json]
                       [schema.core :as s
                        :include-macros true]
                       [schema.utils :as su]
                       [clojure.walk :refer [postwalk]]
                       [xapi-schema.support.data :as d])])
  #?(:clj (:require [speclj.core :refer :all]
                    [schema.core :as s]
                    [schema.utils :as su]
                    [xapi-schema.schemata.util :refer :all]
                    [xapi-schema.schemata.json :as json]
                    [clojure.walk :refer [postwalk]]
                    [xapi-schema.support.data :as d])))

(describe
 "leaves-and-paths"
 (it "returns a map of leaves to paths"
     (should= {"h" ["a" "e" "f" "g"]
               "m" ["a" "e" "f" "l"]
               "d" ["a" "b" "c"]
               "j" ["i"]}
              (leaves-and-paths {"a"{"e" {"f" {"g" "h"
                                               "l" "m"}}
                                     "b" {"c" "d"}}
                                 "i" "j"})))
 (it "consumes all nested data"
     (should= {"h" [0 "a" "e" "f" "g"]
               "m" [0 "a" "e" "f" "l"]
               "d" [0 "a" "b" "c"]
               "j" [0 "i"]
               "n" [0 "a" "b" "k" 0]
               "o" [0 "a" "b" "k" 1]
               "q" [0 "a" "b" "k" 2 "p"]}
              (leaves-and-paths [{"a"{"e" {"f" {"g" "h"
                                                "l" "m"}}
                                      "b" {"c" "d"
                                           "k" ["n" "o" {"p" "q"}]}}
                                  "i" "j"}]))))

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

(describe
 "error fns"

 (describe
  "error-val->json"
  (it "converts values to JSON safe types or keywords"
      (should= [:foo "bar" 1 1.0 nil true false
                #?(:clj "1/2")
                [] []
                {"foo" "bar" "baz" "quxx"}]
       (error-val->json
        [;; will survive
         :foo "bar" 1 1.0 nil true false

         ;; to string
         #?(:clj 1/2)

         ;; to vec
         #{} (list)

         ;; json map
         {:foo "bar" :baz "quxx"}]))))

 (context
  "error processing"

  (with schema
        {(s/required-key "foo") s/Str
         (s/required-key "bar") s/Num
         (s/required-key "baz") s/Int
         (s/required-key "quxx") s/Bool
         (s/required-key "map") {}
         (s/required-key "string-seq") [s/Str]
         (s/required-key "not-there") s/Any
         (s/required-key "equals") (s/eq "foo")
         (s/required-key "enum") (s/enum "foo" "bar" "baz")
         (s/required-key "one") [(s/one s/Str :predicates/at-least-one-agent)]})

  (with err (s/check @schema {"foo" 1
                              "bar" true
                              "baz" 1.1
                              "quxx" "foo"
                              "map" []
                              "string-seq" {}
                              "unknown-key" "hey"
                              "equals" "bar"
                              "enum" "quxx"
                              "one" []
                              }))

  (describe
   "named-error?"
   (it "returns true if the error is a named error"
       (should (named-error? (s/check (s/named s/Str "foo") 1)))))

  (describe
   "validation-error?"
   (it "returns true if the error is a validation error"
       (should (validation-error? (s/check s/Str 1)))))

  (describe
   "errors->json"
   (context
     "with nested named and validation errors"
     (it "converts all error objects to data"
         (should-not-throw
          (postwalk
           (fn [node]
             (if (or (named-error? node)
                     (validation-error? node))
               (throw (#?(:clj Exception.
                          :cljs js/Error.) "error obj found!"))
               node))
           (errors->json @err))))
     (it "converts all predicate and scalar errors to strings"
         (should= {"unknown-key" "Key not allowed"
                   "not-there" "Missing required key"
                   "string-seq" ["not" ["sequential?" {}]]
                   "map" ["not" ["map?" []]]
                   "quxx" ["not" ["boolean?" "foo"]]
                   "baz" ["not" ["integer?" 1.1]]
                   "bar" ["not" ["number?" true]]
                   "foo" ["not" ["string?" 1]]
                   "equals" ["not" ["equal to" "foo" "bar"]]
                   "enum" ["not" ["in" ["foo" "bar" "baz"] "quxx"]]
                   "one" [["not" ["present?" "at least one Agent"]]]}
                  (errors->json @err))))
   (context
    "given some xapi validation cases"
    (context
     "bad timestamp"
     (with bad-stamp
           (errors->json
            (s/check json/Statement
                     (assoc d/long-statement
                            "timestamp"
                            "foo"))))
     (it "parses an agent objectType error"
         (should-not-throw
          @bad-stamp))
     (it "coerces the errors to strings"
         (should= {"timestamp"
                   ["not"
                    ["a valid ISO 8601 timestamp"
                     "foo"
                     ["reason"
                      ["not"
                       ["found in RegEx"
                        "#\"^([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)|T23:59:60Z)?)?$\""
                        "foo"]]]]]}
                  @bad-stamp)))

    (context
     "bad agent type"
     (with bad-agent-type
           (errors->json
            (s/check json/Statement
                     (assoc d/long-statement
                            "actor"
                            {"mbox" "mailto:milt@yetanalytics.com"
                             "objectType" "NotAnAgent"}))))
     (it "parses an agent objectType error"
         (should-not-throw
          @bad-agent-type))
     (it "coerces the errors to strings"
         (should= {"actor" {"objectType"
                            ["not" ["equal to" "Agent" "NotAnAgent"]]}}
                  @bad-agent-type)))

    (context
     "Anon group with no members"
     (with bad-anon-group
           (errors->json
            (s/check json/Statement
                     (assoc d/long-statement
                            "actor"
                            {"objectType" "Group"
                             "member" []}))))
     (it "returns a localized error"
         (should= {"actor" {"member" [["not" ["present?" "at least one Agent"]]]}}
                  @bad-anon-group)))))
  (describe
   "errors->data"
   (it "walks errors and returns their clojure data representation"
       (should=
        {"baz"
         (list (symbol "not") (list (symbol #?(:clj "integer?"
                                               :cljs "cljs$core$integer?")) 1.1))

         "not-there"
         (symbol "missing-required-key")

         "equals"
         (list (symbol "not") (list (symbol "=") "foo" "bar"))

         "quxx"
         (list (symbol "not") (list (symbol "instance?") #?(:clj java.lang.Boolean
                                                            :cljs js/Boolean) "foo"))

         "foo"
         (list (symbol "not") #?(:clj (list (symbol "instance?") java.lang.String 1)
                                 :cljs (list (symbol "cljs$core$string?") 1)))

         "bar"
         (list (symbol "not") (list (symbol "instance?") #?(:clj java.lang.Number
                                                            :cljs js/Number) true))

         "string-seq"
         (list (symbol "not") (list (symbol "sequential?") {}))

         "one"
         [(list (symbol "not") (list (symbol "present?") :predicates/at-least-one-agent))]

         "map"
         (list (symbol "not") (list (symbol "map?") []))

         "enum"
         (list (symbol "not") (list #{"foo" "bar" "baz"} "quxx"))

         "unknown-key" (symbol "disallowed-key")
         }
        (errors->data @err))))
  (describe
   "errors->paths"
   (it "returns a map"
       (should (map? (errors->paths @err))))
   (it "maps errors to their paths"
       (should= {(list (symbol "not") (list (symbol #?(:clj "integer?"
                                                       :cljs "cljs$core$integer?")) 1.1))
                 ["baz"]

                 (symbol "missing-required-key")
                 ["not-there"]

                 (list (symbol "not") (list (symbol "=") "foo" "bar"))
                 ["equals"]

                 (list (symbol "not") (list (symbol "instance?") #?(:clj java.lang.Boolean
                                                                    :cljs js/Boolean) "foo"))
                 ["quxx"]

                 (list (symbol "not") #?(:clj (list (symbol "instance?") java.lang.String 1)
                                         :cljs (list (symbol "cljs$core$string?") 1)))
                 ["foo"]

                 (list (symbol "not") (list (symbol "instance?") #?(:clj java.lang.Number
                                                                    :cljs js/Number) true))
                 ["bar"]

                 (list (symbol "not") (list (symbol "sequential?") {}))
                 ["string-seq"]

                 (list (symbol "not") (list (symbol "present?") :predicates/at-least-one-agent))
                 ["one" 0]

                 (list (symbol "not") (list (symbol "map?") []))
                 ["map"]

                 (list (symbol "not") (list #{"foo" "bar" "baz"} "quxx"))
                 ["enum"]

                 (symbol "disallowed-key") ["unknown-key"]
                 }
                (errors->paths @err))))))
