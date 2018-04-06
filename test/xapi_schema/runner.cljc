(ns xapi-schema.runner
  #?(:cljs (:require [doo.runner :refer-macros [doo-tests]]
                     [xapi-schema.core-test]
                     [xapi-schema.spec-test]
                     [xapi-schema.spec.regex-test]
                     [xapi-schema.spec.resources-test])))

#?(:cljs (doo-tests 'xapi-schema.core-test
                    'xapi-schema.spec-test
                    'xapi-schema.spec.regex-test
                    'xapi-schema.spec.resources-test
                    ))
