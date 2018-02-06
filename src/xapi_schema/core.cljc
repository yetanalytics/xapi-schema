(ns xapi-schema.core
  (:require
   [clojure.spec.alpha :as s :include-macros true]
   [xapi-schema.spec :as xapispec]
   #?(:clj [cheshire.core :as c])))

(def statement-checker
  (partial s/explain-data ::xapispec/statement))

(def statements-checker
  (partial s/explain-data ::xapispec/statements))

(defn validate-statement [s]
  (if (s/valid? ::xapispec/statement s)
    s
    (throw
     (ex-info "Statement Invalid"
              {:type ::statement-invalid
               :statement s
               :error (statement-checker s)}))))

(defn validate-statements [ss]
  (if (s/valid? ::xapispec/statements ss)
    ss
    (throw
     (ex-info "Statements Invalid"
              {:type ::statements-invalid
               :statements ss
               :error (statements-checker ss)}))))

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

#?(:cljs
   (defn ^:export validate-statement-data-js
     [sd]
     (clj->js (validate-statement-data (js->clj sd)))))
