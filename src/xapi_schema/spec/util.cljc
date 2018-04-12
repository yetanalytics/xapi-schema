(ns xapi-schema.spec.util
  (:require [clojure.spec.alpha :as s :include-macros true]
            [clojure.spec.gen.alpha :as sgen :include-macros true])
  #?(:cljs (:require-macros [xapi-schema.spec.util :refer [with-conformer]])))

#?(:clj (defmacro with-conformer
          "Given a base spec and functions to con/unform, return a spec that
           will use
           "
          [spec conform-fn unform-fn & [gen]]
          `(s/with-gen (s/and
                        (s/conformer ~conform-fn ~unform-fn)
                        ~spec)
             #(sgen/fmap
               ~unform-fn
               ~(or
                 gen
                 `(s/gen ~spec))))))
