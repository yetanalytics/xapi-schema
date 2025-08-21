(ns xapi-schema.core-test
  (:require
   [clojure.test :refer [deftest is testing] :include-macros true]
   [xapi-schema.support.data :as d :refer [long-statement]]
   [xapi-schema.core :refer [statement-checker
                             statements-checker
                             validate-statement
                             validate-statements
                             validate-statement-data
                             #?(:cljs validate-statement-data-js)]]
   #?(:clj [clojure.data.json :as json]
      :cljs [cljs.core :refer [ExceptionInfo]]))
  #?(:clj (:import [clojure.lang ExceptionInfo])))

(deftest statement-checker-test
  (testing "with a valid statement"
    (is (nil? (statement-checker long-statement))))

  (testing "with an invalid statement"
    (is (not (nil? (statement-checker (dissoc long-statement "object")))))))

(deftest statements-checker-test
  (testing "with all valid statements"
    (is (nil? (statements-checker [long-statement
                                   long-statement
                                   long-statement]))))

  (testing "with any invalid statements"
    (is (not (nil? (statements-checker [long-statement
                                        long-statement
                                        (dissoc long-statement "object")]))))))

(deftest validate-statement-test
  (testing "with a single statement"
    (testing "with a valid statement in edn"
      (is (= long-statement (validate-statement long-statement))))
    (testing "with an invalid statement"
      (is (= :xapi-schema.core/statement-invalid
             (try (validate-statement {"bad" "statement"})
                  (catch ExceptionInfo ei
                    (some-> ei ex-data :type))))))))

(deftest validate-statements-test
  (testing "with multiple statements"
    (testing "with a valid statement in edn"
      (is (= (vector long-statement) (validate-statements (vector long-statement)))))
    (testing "with invalid statements"
      (is (= :xapi-schema.core/statements-invalid
             (try (validate-statements [{"bad" "statement"}])
                  (catch ExceptionInfo ei
                    (some-> ei ex-data :type))))))))

(deftest validate-statement-data-test
  (testing "with a single statement"
    (testing "with a valid statement in edn"
      (is (= long-statement (validate-statement-data long-statement))))
    (testing "with an invalid statement"
      (is (= :xapi-schema.core/statement-invalid
             (try (validate-statement-data {"bad" "statement"})
                  (catch ExceptionInfo ei
                    (some-> ei ex-data :type)))))))

  (testing "with multiple statements"
    (testing "with valid statements in edn"
      (is (= (vector long-statement) (validate-statement-data (vector long-statement)))))
    (testing "with invalid statements"
      (is (= :xapi-schema.core/statements-invalid
             (try (validate-statement-data [{"bad" "statement"}])
                  (catch ExceptionInfo ei
                    (some-> ei ex-data :type)))))))

  #?(:clj
     (testing "with string data"
       (let [statement (json/write-str long-statement)]
         (testing "it parses and returns the validated data"
           (is (= long-statement (validate-statement-data statement)))))) 
     :cljs
     (testing "with string data"
       (let [json     (clj->js long-statement)
             json-str (.stringify js/JSON json)]
         (testing "it parses and returns the validated data"
           (is (= long-statement (validate-statement-data json-str))))))) 

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
