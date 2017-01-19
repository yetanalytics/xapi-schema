(ns xapi-schema.core-test
  #?@(:cljs (:require [cljs.test :refer-macros [deftest is testing run-tests]]))
  #?@(:cljs [(:require-macros [speclj.core :refer [testing
                                                   it
                                                   should
                                                   should=
                                                   should-not
                                                   testing
                                                   with
                                                   should-be-nil
                                                   should-not-be-nil
                                                   should-throw]])
             (:require [speclj.core]
                       [speclj.platform]
                       [speclj.components]
                       [speclj.run.standard :refer [run-specs]]
                       [xapi-schema.core :refer [statement-checker
                                                 statements-checker
                                                 validate-statement
                                                 validate-statements
                                                 validate-statement-data*
                                                 validate-statement-data
                                                 validate-statement-data-js]]
                       [xapi-schema.support.data :as d :refer [long-statement]])]))

(deftest statement-checker 
 (testing "with a valid statement"
          (is (nil? (statement-checker long-statement))))

 (testing "with an invalid statement"
          (it "returns an error"
              (should-not-be-nil (statement-checker (dissoc long-statement "object"))))))

(testing
 "statements-checker"
 (testing "with all valid statements"
          (it "returns nil"
              (should-be-nil (statements-checker [long-statement
                                                  long-statement
                                                  long-statement]))))
 (testing "with any invalid statements"
          (it "returns an error"
              (should-not-be-nil (statements-checker [long-statement
                                                      long-statement
                                                      (dissoc long-statement "object")])))))
(testing
 "validate-statement"
 (testing "with a valid statement in edn"
          (with statement long-statement)
          (it "returns the statement"
              (should= @statement (validate-statement @statement))))
 (testing "with an invalid statement in edn"
          (with statement (dissoc long-statement "object"))
          (it "throws an error"
              #?(:clj (should-throw (validate-statement @statement))
                 :cljs (should-throw js/Error (validate-statement @statement))))))

(testing
 "validate-statements"
 (testing "with any valid statements in edn"
          (with statements [long-statement
                            long-statement])
          (it "returns the statements"
              (should= @statements (validate-statements @statements))))
 (testing "with any invalid statements in edn"
          (with statements [long-statement (dissoc long-statement "object")])
          (it "throws an error"
              #?(:clj (should-throw (validate-statements @statements))
                 :cljs (should-throw js/Error (validate-statements @statements))))))

(testing
 "validate-statement-data*"
 (testing "with a valid statement"
          (with statement long-statement)
          (it "returns the statement"
              (should= @statement (validate-statement-data* @statement))))
 (testing "with valid statements"
          (with statements [long-statement
                            long-statement])
          (it "returns the statements"
              (should= @statements (validate-statement-data* @statements)))))

(testing
 "validate-statement-data"
 #?(:clj (testing "with string data"
                (with statement (c/generate-string long-statement))
                (it "parses and returns the validated data"
                    (should= long-statement (validate-statement-data @statement))))
    :cljs (testing "with nested data"
                 (with statement long-statement)
                 (it "coerces and returns the data"
                     (should= long-statement (validate-statement-data @statement))))))

#?(:cljs
   (testing
    "validate-statement-data-js"
    (with js-statement (clj->js long-statement))
    (it "returns js data"
        (should (aget @js-statement "id")) ;;assert it is a js obj to begin
        (should= (long-statement "id")
                 (aget (validate-statement-data-js @js-statement) "id")))))

