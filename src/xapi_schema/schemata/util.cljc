(ns xapi-schema.schemata.util
  #?(:clj (:require
           [schema.core :as s]
           [schema.utils :as su]
           [clojure.core.match :refer [match]]
           [clojure.walk :refer [prewalk]]
           [xapi-schema.schemata.i18n :refer [t]]
           [clojure.string :as string])
     :cljs (:require
            [schema.core :as s :include-macros true]
            [schema.utils :as su]
            [cljs.core.match :refer-macros [match]]
            [clojure.walk :refer [prewalk]]
            [xapi-schema.schemata.i18n :refer [t]]
            [clojure.string :as string])))


(defn map->flatmap
  ([map] (map->flatmap [] map))
  ([path map]
   (reduce-kv (fn [m k v]
                (let [prefixed-path (conj path k)]
                  (merge m (if (or (map? v)
                                   (vector? v))
                             (map->flatmap prefixed-path v)
                             {prefixed-path v}))))
              {}
              map)))

(defn flipmap [m]
  (into {}
        (for [[k v] m]
          [v k])))

(def leaves-and-paths
  (comp flipmap map->flatmap))

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

(defn- value-error [e & [ltag]]
  (match [e]
         [([(what :guard keyword?) value] :seq)] (str (t ltag what) ": " value)
         [(['sequential? value] :seq)] (str (t ltag :sequential) ": " value)
         [(['map? value] :seq)] (str (t ltag :map) ": " value)
         [#?(:clj
             (['integer? value] :seq)
             :cljs
             (['cljs$core$integer? value] :seq))] (str (t ltag :integer) ": " value)

         #?@(:cljs
             [[(['cljs$core$string? value] :seq)]
              (str (t ltag :string) ": " value)])

         [(['instance? klass value] :seq)]
         (str #?(:clj (cond
                        (= klass java.lang.String)
                        (str (t ltag :string) ": ")
                        (= klass java.lang.Number)
                        (str (t ltag :number) ": ")
                        (= klass java.lang.Boolean)
                        (str (t ltag :boolean) ": ")
                        :else
                        "unknown instance? predicate: ")
              :cljs (cond
                      (= klass s/Num)
                      (str (t ltag :number) ": ")
                      (= klass s/Bool)
                      (str (t ltag :boolean) ": ")
                      :else
                      "unknown instance? predicate : "))
              value)
         [(['= what value] :seq)]
         (str what ": " value)

         [([(in :guard set?) value] :seq)]
         (str (t ltag :in) " " in ": " value)

         [(['present? required] :seq)]
         (str (t ltag :present) ": " required)

         ;; predicate with string message
         [([(pred-msg :guard string?) value] :seq)]
         (str pred-msg ": " value)
         :else
         e))

(defn named-error? [object]
  (instance? schema.utils.NamedError object))

(defn validation-error? [object]
  (instance? schema.utils.ValidationError object))

(defn error->string [e & [ltag]]
  (let [e (if (validation-error? e)
            (su/validation-error-explain e)
            e)
        ltag (or ltag :en)
        not-x (fn [x] (str (string/capitalize (t ltag :not)) " " x))]
    (match [e]
           ['missing-required-key] (t ltag :missing-required-key)
           ['disallowed-key] (t ltag :disallowed-key)
           [(['not what] :seq)]
           (not-x (value-error what ltag))
           [(['throws? what]:seq)]
           (str (not-x (value-error what ltag)) " (" (t ltag :threw) ")")
           :else
           e)))


(defn- strip-named [node]
  (if (named-error? node)
    (let [name #?(:clj (.name node) :cljs (.-name node))
          error #?(:clj (.error node) :cljs (.-error node))]
      (strip-named error))
    node))

(defn errors->data
  "return errors in the shape of validated data"
  [e & [ltag]]
  (let [ltag (or ltag
                 :en)]
    (prewalk
     (fn [node]
       (error->string
        (strip-named node)
        ltag))
     e)))

(defn errors->paths
  "return a map of errors to their (vector) paths"
  [e & [ltag]]
  (leaves-and-paths (errors->data e ltag)))
