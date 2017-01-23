(ns xapi-schema.runner
  #?(:cljs (:require [doo.runner :refer-macros [doo-tests]]
                     [xapi-schema.core-test]
                     [xapi-schema.schemata.json-test]
                     [xapi-schema.schemata.predicates-test]
                     [xapi-schema.schemata.regex-test]
                     [xapi-schema.schemata.util-test])))

#?(:cljs (doo-tests 'xapi-schema.core-test
                    'xapi-schema.schemata.json-test
                    'xapi-schema.schemata.predicates-test
                    'xapi-schema.schemata.regex-test
                    'xapi-schema.schemata.util-test))
