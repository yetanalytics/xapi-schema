(ns xapi-schema.core-test
  (:require
   [xapi-schema.spec :as xs]
   #?@(:cljs [[cljs.test :refer-macros [deftest is testing run-tests]]
              [xapi-schema.core :refer [statement-checker
                                        statements-checker
                                        validate-statement
                                        validate-statements
                                        validate-statement-data*
                                        validate-statement-data
                                        validate-statement-data-js]]
              [xapi-schema.support.data :as d :refer [long-statement]]]
       :clj [[clojure.test :refer :all]
             [xapi-schema.core :refer :all]
             [xapi-schema.support.data :as d :refer [long-statement]]
             [cheshire.core :as c]])))

(deftest statement-checker-test
  (testing "with a valid statement"
    (is (nil? (statement-checker long-statement)))
    (is (nil? (xs/statement-checker long-statement))))

  (testing "with an invalid statement"
    (is (not (nil? (statement-checker (dissoc long-statement "object")))))
    (is (not (nil? (xs/statement-checker (dissoc long-statement "object")))))))

(deftest statements-checker-test
  (testing "with all valid statements"
    (is (nil? (statements-checker [long-statement
                                   long-statement
                                   long-statement])))
    (is (nil? (xs/statements-checker [long-statement
                                      long-statement
                                      long-statement]))))

  (testing "with any invalid statements"
    (is (not (nil? (statements-checker [long-statement
                                        long-statement
                                        (dissoc long-statement "object")]))))
    (is (not (nil? (xs/statements-checker [long-statement
                                           long-statement
                                           (dissoc long-statement "object")]))))))

(deftest validate-statement-test
  (testing "with a single statement"
    (testing "with a valid statement in edn"
      (is (= long-statement (validate-statement long-statement)))
      (is (= long-statement (xs/validate-statement long-statement))))
    (testing "with invalid statement in clj"
      #?@(:clj [(is (= "error" (try (validate-statement :bad)
                                    (catch Exception e "error"))))
                (is (= "error" (try (xs/validate-statement :bad)
                                    (catch Exception e "error"))))]))
    (testing "with invalid statement in cljs"
      #?@(:cljs [(is (= "error" (try (validate-statement :bad)
                                     (catch js/Error e "error"))))
                 (is (= "error" (try (xs/validate-statement :bad)
                                     (catch js/Error e "error"))))]))))

(deftest validate-statements-test
  (testing "with multiple statements"
    (testing "with a valid statement in edn"
      (is (= (vector long-statement) (validate-statements (vector long-statement))))
      (is (= (vector long-statement) (xs/validate-statements (vector long-statement)))))
    (testing "with invalid statement in clj"
      #?@(:clj [(is (= "error" (try (validate-statements (vector :bad))
                                    (catch Exception e "error"))))
                (is (= "error" (try (xs/validate-statements (vector :bad))
                                    (catch Exception e "error"))))]))
    (testing "with invalid statement in cljs"
      #?@(:cljs [(is (= "error" (try (validate-statements (vector :bad))
                                     (catch js/Error e "error"))))
                 (is (= "error" (try (xs/validate-statements (vector :bad))
                                     (catch js/Error e "error"))))]))))

(deftest validate-statement-data-test
  (testing "with a single statement"
    (testing "with a valid statement in edn"
      (is (= long-statement (validate-statement-data long-statement))))
    (testing "with invalid statement in clj"
      #?(:clj (is (= "error" (try (validate-statement-data :bad)
                                  (catch Exception e "error"))))))
    (testing "with invalid statement in cljs"
      #?(:cljs (is (= "error" (try (validate-statement-data :bad)
                                   (catch js/Error e "error")))))))

  (testing "with multiple statements"
    (testing "with a valid statement in edn"
      (is (= (vector long-statement) (validate-statement-data (vector long-statement)))))
    (testing "with invalid statement in clj"
      #?(:clj (is (= "error" (try (validate-statement-data (vector :bad))
                                  (catch Exception e "error"))))))
    (testing "with invalid statement in cljs"
      #?(:cljs (is (= "error" (try (validate-statement-data (vector :bad))
                                   (catch js/Error e "error")))))))

  #?(:clj
     (testing "with string data"
       (let [statement (c/generate-string long-statement)]
         (testing "it parses and returns the validated data"
           (is (= long-statement (validate-statement-data statement)))))))

  #?(:cljs
     (testing "with nested data"
       (let [statement long-statement]
         (testing "it coerces and returns the data"
           (is (= long-statement (validate-statement-data statement))))))))

#?(:cljs
   (deftest validate-statement-data-js-test
     (testing "with a JS object"
       (let [js-statement (clj->js long-statement)]
         (is (= (long-statement "id") (aget js-statement "id"))) ; just verifying this is a JS object
         (is (= (long-statement "id") (aget (validate-statement-data-js js-statement) "id")))))))
