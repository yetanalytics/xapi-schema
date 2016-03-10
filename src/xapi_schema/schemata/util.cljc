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
            [clojure.walk :refer [prewalk stringify-keys]]
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

;; Error processing

(defn named-error? [object]
  (instance? schema.utils.NamedError object))

(defn validation-error? [object]
  (instance? schema.utils.ValidationError object))

(defn error-val->json [v]
  (if (coll? v)
    (cond (or (vector? v)
            (set? v)
            (list? v))
          (mapv error-val->json v)

          (map? v) (into
                    {}
                    (for [[k v'] v]
                      [(if (keyword? k)
                         (name k)
                         (str k)) (error-val->json v')]))

          :else (str v))
    (if (or
         (keyword? v)
         (string? v)
         #?@(:clj [(float? v)
                   (integer? v)]
             :cljs [(number? v)])
         (nil? v)
         (true? v)
         (false? v))
      v
      (str v))))


(defn error->vec [[problem [pred & values :as expr] :as error]]
  [(keyword problem)
   (match [expr]
          [([(in :guard set?) value] :seq)]
          [:in (error-val->json in) (error-val->json value)]

          ;; handle basic types
          [(['instance? klass value] :seq)]
          [#?(:clj (cond
                     (= klass java.lang.String) :string?

                     (= klass java.lang.Number) :number?

                     (= klass java.lang.Boolean) :boolean?

                     :else
                     :unknown-type)
              :cljs (cond
                      (= klass s/Num) :number?
                      (= klass s/Bool) :boolean?
                      :else
                      :unknown-type))
           (error-val->json value)]


          ;; handle integers
          [#?(:clj
              (['integer? value] :seq)
              :cljs
              (['cljs$core$integer? value] :seq))]
          [:integer? (error-val->json value)]

          ;;strings on cljs
          #?@(:cljs
              [[(['cljs$core$string? value] :seq)]
               [:string? (error-val->json value)]])
          :else
          (into [(keyword pred)] (map error-val->json) values))])


(defn errors->data
  "Walk checker errors and coerce them to data (w/symbols etc. intact)"
  [e]
  (prewalk
   (fn walk-node [e]
     (cond-> e
       (named-error? e) su/named-error-explain
       (validation-error? e) su/validation-error-explain))
   e))

(defn errors->json
  "return errors in the shape of validated data, suitable for json
   and localize errors"
  [e & [ltag]]
  (let [ltag (or ltag
                 :en)]
    (prewalk
     (fn walk-node [node]
       (cond
         (named-error? node)
         ;; We use named schema for scalar checks like RegEx
         ;; here we pull out key regexes and just return the value
         ;; otherwise, we strip the named error and continue.
         (let [ename (.-name node)
               ;; local-name (t ltag ename)
               error (.-error node)]
           (assert (keyword? ename) "Schema names must be keywords")


           (if (validation-error? error)
             (if (= "valid-key" (namespace ename))
               ;; pull the value and make an error string
               (error-val->json (.-value error))

               (walk-node [:not
                           [ename
                            (error-val->json (.-value error))
                            [:reason
                             error]]]))
             (walk-node [:not
                         [ename
                          error]])))

         (validation-error? node)
         (-> node
             su/validation-error-explain
             error->vec)

         ;; convert walked error lists into vectors
         (list? node) (vec node)

         ;; catch single symbols like required-key
         (symbol? node) (-> node keyword (->> errors->json))

         ;; translate all keywords
         (keyword? node) (t ltag node)
         :else node))
     e)))


(defn errors->paths
  "return a map of errors to their (vector) paths"
  [e]
  (leaves-and-paths (errors->data e)))
