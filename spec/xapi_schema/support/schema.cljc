(ns xapi-schema.support.schema
  #?(:clj (:require
           [schema.core :as s]
           [schema.utils :as su]
           [speclj.core :refer [context it should-be-nil should-not-be-nil should-not-contain]]))
  #?@(:cljs [(:require
              [speclj.core]
              [schema.core :as s :include-macros true]
              [schema.utils :as su])
             (:require-macros [speclj.core :refer [context it should-be-nil should-not-be-nil should-not-contain]])]))


(defn should-satisfy [schema data]
  (let [checked (s/check schema data)]
    (should-be-nil checked)))

(defn should-not-satisfy [schema data]
  (let [checked (s/check schema data)]
    (should-not-be-nil checked)))

(defn should-satisfy+
  [schema & goods-bads]
  (let [[goods _ bads] (partition-by #(= :bad %) goods-bads)
        checked-bad (when bads (s/check [schema] bads))]
    (should-be-nil (s/check [schema] goods))
    (when bads
      (should-not-be-nil checked-bad)
      (should-not-contain nil
                          checked-bad))))

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
