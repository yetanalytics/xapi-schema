(ns xapi-schema.support.macros
  (:require [cheshire.core :as c]
            [clojure.java.io :refer [reader]]))

;; Load test json files
(defmacro load-json
  "load a json file as edn (string keys)"
  [path]
  (c/parse-stream (reader path)))

(defmacro load-json-map
  "loads all names (being json files at base-path) into a
   map with the keyworded names for keys"
  [base-path names]
  (into {}
        (for [n names
              :let [path (str base-path n ".json")]]
          [(keyword n) (c/parse-stream (reader path))])))
