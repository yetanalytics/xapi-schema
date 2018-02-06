(ns xapi-schema.runner
  #?(:cljs (:require [doo.runner :refer-macros [doo-tests]]
                     [xapi-schema.core-test]
                     [xapi-schema.spec-test]
                     [xapi-schema.spec.regex-test])))

#?(:cljs (doo-tests 'xapi-schema.core-test
                    'xapi-schema.spec-test
                    'xapi-schema.spec.regex-test
                    ))
