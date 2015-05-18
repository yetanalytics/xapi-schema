(ns xapi-schema.schemata.util
  #+clj (:require
         [schema.core :as s]
         [schema.utils :as su]
         [clojure.core.match :refer [match]])
  #+cljs (:require
          [schema.core :as s :include-macros true]
          [schema.utils :as su]
          [cljs.core.match :refer-macros [match]]))

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
                                     (partition 2 types-and-schemas)))))

(defn match-scalar-error [e]
  #+clj (match [e]
               [([(what :guard string?) (value :guard string?)] :seq)] (str what ": " value)
               [(['sequential? value] :seq)] (str "sequential: " value)
               [(['map? value] :seq)] (str "map: " value)
               [(['integer? value] :seq)] (str "an integer: " value)
               [(['instance? klass value] :seq)] (str (cond
                                                        (= klass java.lang.String) "a string"
                                                        (= klass java.lang.Number) "a number"
                                                        (= klass java.lang.Boolean) "a boolean"
                                                       :else
                                                       "a thing") ": " value)
               :else e)
  #+cljs (match [e]
                ))

(defn match-error [e]
  (match [e]
         ['missing-required-key] "Missing"
         ['disallowed-key] "Not Allowed"
         [(['not what] :seq)] (str "Not " (match-scalar-error what))
         :else e))

(defn error->data [node]
  (cond
    (instance? schema.utils.NamedError node)
    (let [name (.name node)
          error (.error node)]
      (error->data error))
    (instance? schema.utils.ValidationError node)
    (su/validation-error-explain node)
    :else node))

(defn errors->data [e]
  (clojure.walk/postwalk
   (fn [node]
     (match-error node))
   (clojure.walk/prewalk
    error->data
    e)))
