(ns xapi-schema.core
  (:require
   #?@(:clj
       [[schema.core :as s]
        [cheshire.core :as c]])
   #?(:cljs [schema.core :as s
             :include-macros true])
   [xapi-schema.schemata.json :refer [Statement Statements]]
   [schema.utils :as su]
   [xapi-schema.schemata.util :as u]))

;; set a high max value length
(s/set-max-value-length! 10000)

(def statement-checker
  (s/checker Statement))

(def statements-checker
  (s/checker Statements))

;; raw error forms
(def errors->data
  u/errors->data)

;; suitable for json
(def errors->json
  u/errors->json)

(def errors->paths
  u/errors->paths)

(defn validate-statement [s]
  (if-let [error (statement-checker s)]
    (throw
     #?(:clj (Exception. (str error))
        :cljs (js/Error. (str error))))
    s))

(defn validate-statements [ss]
  (if-let [error (statements-checker ss)]
    (throw
     #?(:clj (Exception. (str error))
        :cljs (js/Error. (str error))))
    ss))

(defn validate-statement-data* [sd]
  (if (map? sd)
    (validate-statement sd)
    (validate-statements sd)))

(defn validate-statement-data [sd]
  #?(:clj (validate-statement-data* (cond
                                      (string? sd) (c/parse-string sd)
                                      :else sd))
     :cljs (validate-statement-data*
            (cond
              (string? sd) (.parse js/JSON sd)
              :else sd))))

(defn valid?
  "provides true/false validation for a single statement"
  [stmt]
  (try
    (validate-statement-data stmt)
    true
    (catch #?(:clj Exception
              :cljs js/Error) _
        false)))

#?(:cljs
    (defn ^:export validate-statement-data-js
      [sd]
      (clj->js (validate-statement-data (js->clj sd)))))

#?(:cljs
   (defn ^:export check-statement-data-js
     [sd]
     (let [data (js->clj sd)]
       (clj->js (errors->json ((if (map? data)
                                 statement-checker
                                 statements-checker)
                               data))))))
