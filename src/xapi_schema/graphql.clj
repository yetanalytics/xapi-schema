(ns xapi-schema.graphql
  (:require [clojure.java.io :as io]))

(def schema
  "xAPI graphql base schema, for compilation by Lacinia:
   https://github.com/walmartlabs/lacinia"
  (-> (io/resource "xapi_schema/graphql/schema.edn")
      slurp
      read-string))
