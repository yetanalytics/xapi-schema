(ns xapi-schema.core
  (:require
   [xapi-schema.schemata.json :refer [Statement Statements]]
   #+clj
   [schema.core :as s]
   #+clj [cheshire.core :as c]
   #+cljs [schema.core :as s
           :include-macros true]
   [schema.utils :as su]
   [xapi-schema.schemata.util :as u]))

(def statement-checker
  (s/checker Statement))

(def statements-checker
  (s/checker Statements))

(def errors->data
  u/errors->data)

(def errors->paths
  u/errors->paths)

(defn validate-statement [s]
  (if-let [error (statement-checker s)]
    #+clj (throw (Exception. (str error)))
    #+cljs (throw (js/Error. (str error)))
    s))

(defn validate-statements [ss]
  (if-let [error (statements-checker ss)]
    #+clj (throw (Exception. (str error)))
    #+cljs (throw (js/Error. (str error)))
    ss))

(defn validate-statement-data* [sd]
  (if (map? sd)
    (validate-statement sd)
    (validate-statements sd)))

(defn validate-statement-data [sd]
  #+clj
  (validate-statement-data* (cond
                              (string? sd) (c/parse-string sd)
                              :else sd))
  #+cljs
  (validate-statement-data*
   (cond
     (string? sd) (.parse js/JSON sd)
     :else sd)))

#+cljs
(defn ^:export validate-statement-data-js
  [sd]
  (clj->js (validate-statement-data (js->clj sd))))
