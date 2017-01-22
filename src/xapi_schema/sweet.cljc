(ns xapi-schema.sweet
  (:require [xapi-schema.core :as xapi]))

(defn valid? [stmt]
  (try
   (xapi/validate-statement-data stmt)
   true
   (catch #?(:clj Exception
             :cljs js/Error) _
       false)))
