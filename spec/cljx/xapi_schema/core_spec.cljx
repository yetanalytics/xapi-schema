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
                                             validate-statement-data]]
                   [xapi-schema.support.data :as d])
  #+clj (:require [speclj.core :refer :all]
                  [xapi-schema.core :refer :all]
                  [xapi-schema.support.data :as d]
                  [cheshire.core :as c]))

(describe
 "statement-checker"
 (context "with a valid statement"
          (it "returns nil"
              (should-be-nil (statement-checker d/statement))))
 (context "with an invalid statement"
          (it "returns an error"
              (should-not-be-nil (statement-checker (dissoc d/statement "object"))))))

(describe
 "statements-checker"
 (context "with all valid statements"
          (it "returns nil"
              (should-be-nil (statements-checker [d/statement
                                                  d/statement
                                                  d/statement]))))
 (context "with any invalid statements"
          (it "returns an error"
              (should-not-be-nil (statements-checker [d/statement
                                                      d/statement
                                                      (dissoc d/statement "object")])))))
(describe
 "validate-statement"
 (context "with a valid statement in edn"
          (with statement d/statement)
          (it "returns the statement"
              (should= @statement (validate-statement @statement))))
 (context "with an invalid statement in edn"
          (with statement (dissoc d/statement "object"))
          (it "throws an error"
              #+clj (should-throw (validate-statement @statement))
              #+cljs (should-throw js/Error (validate-statement @statement)))))

(describe
 "validate-statements"
 (context "with any valid statements in edn"
          (with statements [d/statement d/statement])
          (it "returns the statements"
              (should= @statements (validate-statements @statements))))
 (context "with any invalid statements in edn"
          (with statements [d/statement (dissoc d/statement "object")])
          (it "throws an error"
              #+clj (should-throw (validate-statements @statements))
              #+cljs (should-throw js/Error (validate-statements @statements)))))

(describe
 "validate-statement-data*"
 (context "with a valid statement"
          (with statement d/statement)
          (it "returns the statement"
              (should= @statement (validate-statement-data* @statement))))
 (context "with valid statements"
          (with statements [d/statement d/statement])
          (it "returns the statements"
              (should= @statements (validate-statement-data* @statements)))))

(describe
 "validate-statement-data"
 #+clj (context "with string data"
                (with statement (c/generate-string d/statement))
                (it "parses and returns the validated data"
                    (should= d/statement (validate-statement-data @statement))))
 #+cljs (context "with js data"
          (with statement (clj->js d/statement))
          (it "coerces and returns the data"
              (should= d/statement (validate-statement-data @statement)))))
