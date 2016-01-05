(ns xapi-schema.sweet
  (:require [xapi-schema.core :as xapi]))

(defn valid? [stmt]
  (try
   (xapi/validate-statement-data stmt)
   true
   #?(:clj (catch Exception)
      false
      :cljs (catch js/Error)
      false)))
