(ns xapi-schema.spec.resources-test
  (:require [clojure.test :refer [deftest is testing] :include-macros true]
            [clojure.spec.alpha :as s :include-macros true]
            [xapi-schema.spec.resources :as xsr :refer [parse-json
                                                        unparse-json
                                                        json-string-conformer]]))

(deftest parse-json-test
  (is (= {"foo" "bar"}
         (parse-json "{\"foo\":\"bar\"}"))))

(deftest unparse-json-test
  (is (= "{\"foo\":\"bar\"}"
         (unparse-json {"foo" "bar"}))))

(deftest json-string-conformer-test
  (is (= {"foo" "bar"}
         (s/conform json-string-conformer
                    "{\"foo\":\"bar\"}")))
  (is (s/valid? json-string-conformer
                "{\"foo\":\"bar\"}"))
  (is (not
       (s/valid? json-string-conformer
                 "{\"foo\":\"bar\"")))
  (is (= {"foo" "bar"}
         (s/conform json-string-conformer
                    {"foo" "bar"})))
  (is (= "{\"foo\":\"bar\"}"
         (s/unform json-string-conformer
                   {"foo" "bar"})))
  (is (= "{\"foo\":\"bar\"}"
         (s/unform json-string-conformer
                   "{\"foo\":\"bar\"}")))
  )

(deftest agent-param-test
  (is (s/valid? :xapi.common.param/agent
                "{\"mbox\":\"mailto:milt@yetanalytics.com\"}"))
  (is (not (s/valid? :xapi.common.param/agent
                     "{\"mbox\":\"milt@yetanalytics.com\"}")))
  (is (not (s/valid? :xapi.common.param/agent
                     "{\"email\":\"mailto:milt@yetanalytics.com\"}"))))

(deftest statements-get-params-test
  (is (s/valid? :xapi.statements.GET.request/params
                {"statementId" (str #?(:clj (java.util.UUID/randomUUID)
                                       :cljs (random-uuid)))
                 "format" "ids"}))
  #_(is (not
       (s/valid? :xapi.statements.GET.request/params
                 {"statementId" (str #?(:clj (java.util.UUID/randomUUID)
                                        :cljs (random-uuid)))
                  "ascending" true})))
  (is (s/valid? :xapi.statements.GET.request/params
                {"ascending" true
                 "format" "ids"})))
