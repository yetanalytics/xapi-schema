(ns xapi-schema.schemata.util
  #+clj (:require
         [schema.core :as s]
         [schema.utils :as su]
         [clojure.core.match :refer [match]]
         [clojure.walk :refer [prewalk postwalk]]
         [clojure.zip :as zip])
  #+cljs (:require
          [schema.core :as s :include-macros true]
          [schema.utils :as su]
          [cljs.core.match :refer-macros [match]]
          [clojure.walk :refer [prewalk postwalk]]
          [clojure.zip :as zip]))




(defn map-zipper [m]
  (zip/zipper
   (fn [x] (or (map? x) (map? (nth x 1))))
   (fn [x] (seq (if (map? x) x (nth x 1))))
   (fn [x children]
     (if (map? x)
       (into {} children)
       (assoc x 1 (into {} children))))
   m))

(defn vec-map [data]
  (into {}
        (map-indexed
         (fn [idx item]
           [idx item])
         data)))

(defn leaves-and-paths [data]
  (loop [mzip (map-zipper (if (vector? data)
                            (vec-map data)
                            data))
           l-p-map {}]
      (cond
        (nil? mzip) l-p-map
        (zip/branch? mzip) (recur
                            (if (= {} (second (zip/node mzip)))
                              (zip/remove mzip)
                              (zip/down mzip))
                            l-p-map)
        (vector? (second (zip/node mzip)))
        (recur (zip/edit mzip (fn [[k v]]
                                [k (vec-map v)]))
               l-p-map)
        :else
        (let [[k v] (zip/node mzip)
              path (conj
                    (mapv first (rest (zip/path mzip)))
                    k)]
          (recur
           (zip/remove mzip)
           (assoc l-p-map
                  v path))))))


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

(defn- value-error [e]
  (match [e]
         [([(what :guard string?) value] :seq)] (str what ": " value)
         [(['sequential? value] :seq)] (str "sequential: " value)
         [(['map? value] :seq)] (str "map: " value)
         [(['integer? value] :seq)] (str "an integer: " value)

         ;; catch cljs string schema, as it is just string?
         #+cljs
         [([(what :guard (partial = string?)) value] :seq)]
         #+cljs
         (str "a string: " value)

         [(['instance? klass value] :seq)]
         (str (cond
                #+clj (= klass java.lang.String)
                #+clj "a string: "

                #+clj (= klass java.lang.Number)
                #+cljs (= klass s/Num)
                "a number: "
                #+clj (= klass java.lang.Boolean)
                #+cljs (= klass s/Bool)
                "a boolean: "
                :else
                "unknown instance? predicate: ")
              value)
         [(['= what value]:seq)]
         (str what ": " value)

         [([(in :guard set?) value] :seq)]
         (str "in " in ": " value)

         [(['present? required] :seq)]
         (str "present: " required)
         :else e))

(defn error->string [e]
  (match [e]
         ['missing-required-key] "Missing"
         ['disallowed-key] "Not Allowed"
         [(['not what] :seq)] (str "Not " (value-error what))
         [(['throws? what]:seq)] (str "Not " (value-error what) " (threw)")
         :else e))

(defn named-error? [object]
  (instance? schema.utils.NamedError object))

(defn validation-error? [object]
  (instance? schema.utils.ValidationError object))


(defn- error->data [node]
  (cond
    (named-error? node)
    (let [name #+clj (.name node) #+cljs (.-name node)
          error #+clj (.error node) #+cljs (.-error node)]
      (error->data error))
    (validation-error? node)
    (su/validation-error-explain node)
          :else node))

(defn errors->data [e]
  (postwalk
   (fn [node]
     (error->string node))
   (prewalk
    error->data
    e)))

(defn errors->paths [e]
  (leaves-and-paths
   (errors->data e)))
