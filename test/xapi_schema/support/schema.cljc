(ns xapi-schema.support.schema
  #?(:clj (:require
           [schema.core :as s]
           [schema.utils :as su]
           [clojure.test :refer :all]
           [clojure.spec.alpha :as spec]
           [xapi-schema.spec :as xs]))
  #?@(:cljs [(:require
              [cljs.test :refer-macros [deftest is testing run-tests]]
              [schema.core :as s :include-macros true]
              [schema.utils :as su]
              [clojure.spec.alpha :as spec :include-macros true]
              [xapi-schema.spec :as xs])]))

(defn should-satisfy [schema spec data]
  (let [checked-schema (s/check schema data)
        checked-spec (spec/explain-data spec data)]
    (is (nil? checked-schema))
    (is (nil? checked-spec))))

(defn should-not-satisfy [schema spec data]
  (let [checked-schema (s/check schema data)
        checked-spec   (spec/explain-data spec data)]
    (is (not (nil? checked-schema)))
    (is (not (nil? checked-spec)))))

(defn should-satisfy+
  [schema spec & goods-bads]
  (let [[goods _ bads] (partition-by #(= :bad %) goods-bads)
        checked-bad-schema (when bads (s/check [schema] bads))
        checked-bad-spec (when bads (spec/explain-data (spec/coll-of spec) bads))
        ]
    (is (nil? (s/check [schema] goods)))
    (when bads
      (is (not (nil? checked-bad-schema)))
      (is (not (contains? checked-bad-schema nil))))
    (is (nil? (spec/explain-data (spec/coll-of spec) goods)))
    (when bads
      (is (not (nil? checked-bad-spec))))))

(defn key-should-satisfy+ [schema
                           spec
                           base
                           key
                           & goods-bads]
  (let [[goods _ bads] (partition-by #(= :bad %) goods-bads)
        gbs  (concat
              (map
               #(assoc base key %)
               goods)
              '(:bad)
              (map
               #(assoc base key %)
               bads))]
    (apply
     should-satisfy+
     schema
     spec
     gbs)))
