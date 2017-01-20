(ns xapi-schema.core-test
  #?@(:cljs [(:require [cljs.test :refer-macros [deftest is testing run-tests]])
             (:require [xapi-schema.core :refer [statement-checker
                                                 statements-checker
                                                 validate-statement
                                                 validate-statements
                                                 validate-statement-data*
                                                 validate-statement-data
                                                 validate-statement-data-js]]
                       [xapi-schema.support.data :as d :refer [long-statement]])])
  #?(:clj (:require [clojure.test :refer :all]
                     [xapi-schema.core :refer :all]
                     [xapi-schema.support.data :as d :refer [long-statement]]
                     [cheshire.core :as c])))


(deftest statement-validation 
  (testing "with a valid statement"
    (is (nil? (statement-checker long-statement))))

  (testing "with an invalid statement"
    (is (not (nil? (statement-checker (dissoc long-statement "object"))))))

  (testing "statements-checker"
    (testing "with all valid statements"
      (is (nil? (statements-checker [long-statement
                                     long-statement
                                     long-statement])))))

  (testing "with any invalid statements"
    (is (not (nil? (statements-checker [long-statement
                                        long-statement
                                        (dissoc long-statement "object")])))))

  (defn should-validate-one-statement [fun]
    (testing "testing..."
    (testing "with a valid statement in edn"
      (is (= long-statement (fun long-statement))))
    (testing "with invalid statement in clj" 
      #?(:clj (is (= "error" (try (fun :bad)
                             (catch Exception e "error"))))))
    (testing "with invalid statement in cljs" 
      #?(:cljs (is (= "error" (try (fun :bad)
                              (catch js/Error e "error"))))))))

  (defn should-validate-multiple-statements [fun]
    (testing "testing..."
    (testing "with a valid statement in edn"
      (is (= (vector long-statement) (fun (vector long-statement)))))
    (testing "with invalid statement in clj" 
      #?(:clj (is (= "error" (try (fun (vector :bad))
                             (catch Exception e "error"))))))
    (testing "with invalid statement in cljs" 
      #?(:cljs (is (= "error" (try (fun (vector :bad))
                              (catch js/Error e "error"))))))))

  (defn should-work-with-js-data [fun]
    (testing "validate-statement-data-js"
      #?(:cljs 
          (let [js-statement (atom (clj->js long-statement))]
            (is (= "id" (aget @js-statement "id"))) ; just verifying this is a JS object
            (is (= "id" (aget (fun @js-statement) "id")))))))
  
  (-> validate-statement should-validate-one-statement)
  (-> validate-statements should-validate-multiple-statements)
  (-> validate-statement-data should-validate-one-statement)
  (-> validate-statement-data should-validate-multiple-statements)
  (-> validate-statement-data should-work-with-js-data)
  (-> 'validate-statement-data-js should-work-with-js-data))
