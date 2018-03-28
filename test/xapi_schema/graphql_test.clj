(ns xapi-schema.graphql-test
  (:require [clojure.test :refer :all]
            [xapi-schema.graphql :refer :all]
            [xapi-schema.spec] ;; just to get the spec defs
            [com.walmartlabs.lacinia.schema :as ls]
            [clojure.spec.alpha :as s]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :as w]))

(defn- sanitize-schema
  "Until lacinia can validate with knowledge of a defult resolver, we need to
  add dummy functions for :resolve and :stream, and resolve specs for :parse and
  :serialize"
  [raw-schema]
  (w/postwalk
   (fn [node]
     (if-let [[k & [?v & _]] (and
                       (vector? node)
                       node)]
       (cond
         (#{:resolve
            :stream} k)
         [k (constantly nil)]
         (#{:parse
            :serialize} k)
         [k (s/spec ?v)]
         :else node)
       node))
   raw-schema))

(deftest spec-test
  (let [exp-d (s/explain-data ::ls/schema-object (sanitize-schema
                                                  schema))]
    (is (nil? exp-d) "schema should pass Lacinia's spec")
    ;; print it out
    (when exp-d
      (s/explain ::ls/schema-object (sanitize-schema schema)))))

(deftest compile-test
  (is (nil?
       (try (let [compiled-schema
                  (ls/compile (sanitize-schema schema)
                              {:default-field-resolver
                               (constantly nil)})]
              nil)
            (catch clojure.lang.ExceptionInfo exi
              (let [exd (ex-data exi)]
                exd))))
      "schema should compile"))
