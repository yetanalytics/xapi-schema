(ns xapi-schema.schemata.util
  (:require
   #+clj [schema.core :as s]
   #+cljs [schema.core :as s :include-macros true]))

(defn check-type
  "make a predicate to check for a given object type"
  [type-string]
  (fn [data]
    (= type-string (or (get data "objectType") (get data :objectType)))))

(defn object-type-dispatch
  [& types-and-schemas]
  "returns a conditional schema for the given map of objectTypes"

  (apply s/conditional (flatten (map (fn [[obj-type schema]]
                                       (if (= :else obj-type)
                                         [:else schema]
                                         [(check-type obj-type) schema]))
                                     (partition 2 types-and-schemas))))
  )
