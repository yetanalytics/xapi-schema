(ns xapi-schema.schemata.util-test
  #?@(:cljs [(:require [cljs.test :refer-macros [deftest is testing run-tests]]
                       [xapi-schema.schemata.util :refer [check-type
                                                          object-type-dispatch
                                                          error->string
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
  #?(:clj (:require [clojure.test :refer :all]
                    [clojure.core :refer :all]
                    [schema.core :as s]
                    [schema.utils :as su]
                    [xapi-schema.schemata.util :refer :all]
                    [xapi-schema.schemata.json :as json]
                    [clojure.walk :refer [postwalk]]
                    [xapi-schema.support.data :as d])))

(defn should= [arg1 arg2]
  (is (= arg1 arg2)))

(defn should-not [arg]
  (is (not (arg))))

(defn should [arg]
  (is (arg)))

(defn should-not-throw [fun]
  (is (try fun
           (catch #?(:clj Exception
                     :cljs js/Error) e
         false))))

(deftest util-tests
(testing "leaves-and-paths"
 (testing "returns a map of leaves to paths"
     (is (= {"h" ["a" "e" "f" "g"]
               "m" ["a" "e" "f" "l"]
               "d" ["a" "b" "c"]
               "j" ["i"]}
              (leaves-and-paths {"a"{"e" {"f" {"g" "h"
                                               "l" "m"}}
                                     "b" {"c" "d"}}
                                 "i" "j"}))))
 (testing "consumes all nested data"
     (is (= {"h" [0 "a" "e" "f" "g"]
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
                                  "i" "j"}])))))

(testing "check-type"

          (let [pred (check-type "Activity")]
              (testing "returns a predicate that checks for the given objectType"
              (is (pred {"objectType" "Activity"}))
              (is (not (pred {"objectType" "Agent"}))))))

(testing "object-type-dispatch"
          (let [foo-schema {(s/required-key "objectType") s/Str
                            (s/required-key "thing") s/Str}
                bar-schema {(s/required-key "objectType") s/Str
                            (s/required-key "thing") s/Num}
                baz-schema {(s/required-key "thing") s/Keyword}
                foo {"objectType" "foo"
                     "thing" "whatever"}
                bar {"objectType" "bar"
                     "thing" 2}
                baz {"thing" :whatever}
                conditional-schema (object-type-dispatch
                                    "foo" foo-schema
                                    "bar" bar-schema
                                    :else baz-schema)]
           (testing "returns a conditional schema for the given map of object types"
              (is (not (or (s/check conditional-schema
                                       foo)
                              (s/check conditional-schema
                                       bar)
                              (s/check conditional-schema
                                       baz))))
              (is (and
                       (s/check conditional-schema {"objectType" "foo"
                                                     "thing" :whatever})
                       (s/check conditional-schema {"thing" "foo"}))))))

(testing
 "error fns"
 (testing
  "error->string"
  (testing
   "given a missing key error"
   (let [err (s/check {(s/required-key "foo") s/Str} {})]
     (testing "converts it to an English string"
       (is (= "Missing required key"
                (error->string  (get
                                 err
                                 "foo")))))))
  (testing
   "given a disallowed key error"
   (let [err (s/check {} {"foo" "bar"})]
    (testing "converts it to an English string"
       (is (= "Key not allowed"
                (error->string (get
                                err
                                "foo")))))))
  (testing
   "given a NOT validation error"
   (testing
    "from a predicate"
    (let [err
          (s/check (s/pred (fn [bar]
                             (= "bar" bar)) "a bar") "foo")]
     (testing "converts it to an English string"
        (is (= "Not a bar: foo"
                 (error->string err))))))
   (testing
    "from a predicate that throws"
    (let [err (s/check (s/pred seq "a sequable thing") true)]
      (testing "mentions it"
        (is (= "Not a sequable thing: true (threw)"
                 (error->string err))))))
   (testing
    "from a seq schema"
    (let [err (s/check [] {})]
      (testing "converts"
        (is (= "Not sequential: {}"
                 (error->string err))))))
   (testing
    "from a map schema"
    (let [err (s/check {} [])]
      (testing "converts"
        (is (= "Not map: []"
                 (error->string err))))))
   (testing
    "from an integer"
    (let [err (s/check s/Int "foo")]
      (testing "converts"
        (is (= "Not integer: foo"
                 (error->string err))))))
   (testing
    "from s/Str"
    (let [err (s/check s/Str 1)]
     (testing "converts"
        (is (= "Not string: 1"
                 (error->string err))))))
   (testing
    "from s/Num"
    (let [err (s/check s/Num "foo")]
      (testing "converts"
        (is (= "Not number: foo"
                 (error->string err))))))
   (testing
    "from s/Bool"
    (let [err (s/check s/Bool "foo")]
      (testing "converts"
        (is (= "Not boolean: foo"
                 (error->string err))))))))

 (let [schema (s/named
                {(s/required-key "foo") s/Str
                 (s/required-key "bar") s/Num
                 (s/required-key "baz") s/Int
                 (s/required-key "quxx") s/Bool
                 (s/required-key "map") {}
                 (s/required-key "string-seq") [s/Str]
                 (s/required-key "not-there") s/Any
                 (s/required-key "equals") (s/eq "foo")
                 (s/required-key "enum") (s/enum "foo" "bar" "baz")
                 (s/required-key "one") [(s/one s/Str "at least one string")]}
                "bob")
       err (s/check schema {"foo" 1
                              "bar" true
                              "baz" 1.1
                              "quxx" "foo"
                              "map" []
                              "string-seq" {}
                              "unknown-key" "hey"
                              "equals" "bar"
                              "enum" "quxx"
                              "one" []
                              })]
  (testing
  "error processing"
  (testing
   "named-error?"
   (testing "returns true if the error is a named error"
       (is (named-error? (s/check (s/named s/Str "foo") 1)))))

  (testing
   "validation-error?"
   (testing "returns true if the error is a validation error"
       (is (validation-error? (s/check s/Str 1)))))

  (testing
   "errors->data"
   (testing
     "with nested named and validation errors"
     (testing "converts all error objects to data"
         (should-not-throw
          (postwalk
           (fn [node]
             (if (or (named-error? node)
                     (validation-error? node))
               (throw (#?(:clj Exception.
                          :cljs js/Error.) "error obj found!"))
               node))
           (errors->data err))))
     (testing "converts all predicate and scalar errors to strings"
         (= {"unknown-key" "Key not allowed"
                   "not-there" "Missing required key"
                   "string-seq" "Not sequential: {}"
                   "map" "Not map: []"
                   "quxx" "Not boolean: foo"
                   "baz" "Not integer: 1.1"
                   "bar" "Not number: true"
                   "foo" "Not string: 1"
                   "equals" "Not foo: bar"
                   "enum" "Not in #{\"foo\" \"bar\" \"baz\"}: quxx"
                   "one" ["Not present: at least one string"]}
                  (errors->data err))))
    (testing
    "given some xapi validation cases"
    (let [bad-agent-type
          (errors->data
            (s/check json/Statement
                     (assoc d/long-statement
                            "actor"
                            {"mbox" "mailto:milt@yetanalytics.com"
                             "objectType" "NotAnAgent"})))]
     (testing
     "bad agent type"
     (testing "parses an agent objectType error"
         (should-not-throw bad-agent-type)))

     (testing "coerces the errors to strings"
         (is (= {"actor" {"objectType" "Not Agent: NotAnAgent"}}
                  bad-agent-type)))))

    (testing
     "Anon group with no members"
     (let [bad-anon-group
           (errors->data
            (s/check json/Statement
                     (assoc d/long-statement
                            "actor"
                            {"objectType" "Group"
                             "member" []})))]
       (testing "returns a localized error"
         (is (= {"actor" {"member" ["Not present: at least one Agent"]}}
                  bad-anon-group)))))

    ))
  (testing
   "errors->paths"
   (testing "returns a map"
       (is (map? (errors->paths err))))
   (testing "maps errors to their paths"
       (is (= (errors->paths err)
                {"Key not allowed" ["unknown-key"]
                 "Missing required key" ["not-there"]
                 "Not sequential: {}" ["string-seq"]
                 "Not map: []" ["map"]
                 "Not boolean: foo" ["quxx"]
                 "Not integer: 1.1" ["baz"]
                 "Not number: true" ["bar"]
                 "Not string: 1" ["foo"]
                 "Not foo: bar" ["equals"]
                 "Not in #{\"foo\" \"bar\" \"baz\"}: quxx" ["enum"]
                 "Not present: at least one string" ["one" 0]})))))))
