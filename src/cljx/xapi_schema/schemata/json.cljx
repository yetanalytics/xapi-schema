(ns xapi-schema.schemata.json
  (:require
   [xapi-schema.schemata.predicates :as preds]
   [xapi-schema.schemata.regex :as regs]
   #+clj [schema.core :as s]
   #+cljs [schema.core :as s
           :include-macros true]))
