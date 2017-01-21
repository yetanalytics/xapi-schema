(ns xapi-schema.support.schema
  #?(:clj (:require
           [schema.core :as s]
           [schema.utils :as su]
           [clojure.test :refer :all]))
  #?@(:cljs [(:require
              [cljs.test :refer-macros [deftest is testing run-tests]]
              [schema.core :as s :include-macros true]
              [schema.utils :as su])]))

(defn should-satisfy [schema data]
  (let [checked (s/check schema data)]
    (is (nil? checked))))

(defn should-not-satisfy [schema data]
  (let [checked (s/check schema data)]
    (is (not (nil? checked)))))

(defn should-satisfy+
  [schema & goods-bads]
  (let [[goods _ bads] (partition-by #(= :bad %) goods-bads)
        checked-bad (when bads (s/check [schema] bads))]
    (is (nil? (s/check [schema] goods)))
    (when bads
      (is (not (nil? checked-bad)))
      (is (not (contains? checked-bad nil))))))

(defn key-should-satisfy+ [schema
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
     gbs)))
