(ns xapi-schema.core-spec
  #+cljs (:require-macros [speclj.core :refer [describe
                                               it
                                               should
                                               should=
                                               should-not
                                               run-specs
                                               context
                                               with
                                               should-be-nil
                                               should-not-be-nil
                                               should-throw]])
  #+cljs (:require [speclj.core]
                   [xapi-schema.core :refer [statement-checker
                                             statements-checker
                                             validate-statement
                                             validate-statements
                                             validate-statement-data*
                                             validate-statement-data
                                             validate-statement-data-js]]
                   [xapi-schema.support.data :as d :refer [long-statement]])
  #+clj (:require [speclj.core :refer :all]
                  [xapi-schema.core :refer :all]
                  [xapi-schema.support.data :as d :refer [long-statement]]
                  [cheshire.core :as c]))

(describe
 "statement-checker"
 (context "with a valid statement"
          (it "returns nil"
              (should-be-nil (statement-checker long-statement))))
 (context "with an invalid statement"
          (it "returns an error"
              (should-not-be-nil (statement-checker (dissoc long-statement "object"))))))

(describe
 "statements-checker"
 (context "with all valid statements"
          (it "returns nil"
              (should-be-nil (statements-checker [long-statement
                                                  long-statement
                                                  long-statement]))))
 (context "with any invalid statements"
          (it "returns an error"
              (should-not-be-nil (statements-checker [long-statement
                                                      long-statement
                                                      (dissoc long-statement "object")])))))
(describe
 "validate-statement"
 (context "with a valid statement in edn"
          (with statement long-statement)
          (it "returns the statement"
              (should= @statement (validate-statement @statement))))
 (context "with an invalid statement in edn"
          (with statement (dissoc long-statement "object"))
          (it "throws an error"
              #+clj (should-throw (validate-statement @statement))
              #+cljs (should-throw js/Error (validate-statement @statement)))))

(describe
 "validate-statements"
 (context "with any valid statements in edn"
          (with statements [long-statement
                            long-statement])
          (it "returns the statements"
              (should= @statements (validate-statements @statements))))
 (context "with any invalid statements in edn"
          (with statements [long-statement (dissoc long-statement "object")])
          (it "throws an error"
              #+clj (should-throw (validate-statements @statements))
              #+cljs (should-throw js/Error (validate-statements @statements)))))

(describe
 "validate-statement-data*"
 (context "with a valid statement"
          (with statement long-statement)
          (it "returns the statement"
              (should= @statement (validate-statement-data* @statement))))
 (context "with valid statements"
          (with statements [long-statement
                            long-statement])
          (it "returns the statements"
              (should= @statements (validate-statement-data* @statements)))))

(describe
 "validate-statement-data"
 #+clj (context "with string data"
                (with statement (c/generate-string long-statement))
                (it "parses and returns the validated data"
                    (should= long-statement (validate-statement-data @statement))))
 #+cljs (context "with js data"
          (with statement (clj->js long-statement))
          (it "coerces and returns the data"
              (should= long-statement (validate-statement-data @statement)))))

#+cljs (describe
        "validate-statement-data-js"
        (with js-statement (clj->js long-statement))
        (it "returns js data"
            (should (aget @js-statement "id")) ;;assert it is a js obj to begin
            (should= (long-statement "id")
                     (aget (validate-statement-data-js @js-statement) "id"))))
