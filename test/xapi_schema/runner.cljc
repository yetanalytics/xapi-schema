(ns xapi-schema.runner
  (:require [#?(:clj clojure.test
                :cljs cljs.test) :refer [run-tests]]
            xapi-schema.core-test
            xapi-schema.spec-test
            xapi-schema.spec.regex-test
            xapi-schema.spec.resources-test
            #?@(:clj [xapi-schema.graphql-test]
                :cljs [[cljs.nodejs :refer [process]]])))
;; Exit properly for cljs tests
#?(:cljs (defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
           (.exit process
                  (if (cljs.test/successful? m)
                    0
                    1))))

#?(:clj (defn -main []
          (let [{:keys [test pass fail error] :as result}
                (run-tests 'xapi-schema.core-test
                           'xapi-schema.graphql-test
                           'xapi-schema.spec-test
                           'xapi-schema.spec.regex-test
                           'xapi-schema.spec.resources-test)]
            (System/exit (if (= 0 fail error)
                           0
                           1))))
   :cljs (set! *main-cli-fn*
               (fn []
                 (run-tests 'xapi-schema.core-test
                            'xapi-schema.spec-test
                            'xapi-schema.spec.regex-test
                            'xapi-schema.spec.resources-test
                            ))))
