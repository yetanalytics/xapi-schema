(ns xapi-schema.support.spec
  (:require [clojure.test :refer [deftest is testing] :include-macros true]
            [clojure.spec.alpha :as s :include-macros true]))

(defn should-satisfy [spec data]
  (is (nil? (s/explain-data spec data))))

(defn should-not-satisfy [spec data]
  (is (not (nil? (s/explain-data spec data)))))

(defn should-satisfy+
  [spec & goods-bads]
  (let [[goods _ bads] (partition-by #(= :bad %) goods-bads)
        checked-bad-spec (when bads (map (partial s/explain-data spec)
                                         bads))]
    (is (nil? (s/explain-data (s/coll-of spec) goods)))
    (when bads
      (is (not (some nil? checked-bad-spec))))))

(defn key-should-satisfy+ [spec
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
     spec
     gbs)))
