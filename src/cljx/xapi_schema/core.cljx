(ns xapi-schema.core
  (:require
   #+clj [schema.core :as s]
   #+cljs [schema.core :as s
                     :include-macros true]))

(defn foo []
  #+clj "bar"
  #+cljs "bar")
