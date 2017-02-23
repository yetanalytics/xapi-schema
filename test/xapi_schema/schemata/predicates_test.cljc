(ns xapi-schema.schemata.predicates-test
  (:require
   #?@(:cljs [[cljs.test :refer-macros [testing is testing run-tests deftest]]
               [xapi-schema.schemata.predicates :refer [ifi-count
                                                        no-ifi?
                                                        re-pred
                                                        unique-ids?
                                                        valid-component-keys?
                                                        no-multi-ifi?
                                                        ifi-present?
                                                        has-members?
                                                        valid-score?
                                                        two-members?
                                                        valid-revision?
                                                        valid-platform?
                                                        valid-void?]]
               [schema.core :as s
                :include-macros true]
               [xapi-schema.support.schema :refer [should-satisfy
                                                   should-not-satisfy
                                                   should-satisfy+]]]
              :clj [[clojure.test :refer :all]
                    [schema.core :as s]
                    [xapi-schema.schemata.predicates :refer :all]
                    [xapi-schema.support.schema :refer [should-satisfy
                                                        should-not-satisfy
                                                        should-satisfy+]]])))

(deftest ifi-count-test
  (testing "correctly returns the ifi-count"
    (is (= 0 (ifi-count {})))
    (is (= 1 (ifi-count {"foo" "bar"
                         "mbox" "mailto:milt@yetanalytics.com"})))
    (is (= 2 (ifi-count {"foo" "bar"
                         "mbox" "mailto:milt@yetanalytics.com"
                         "account" {"homePage" "http://www.foo.com"
                                    "name" "foobar"}})))))

(deftest no-multi-ifi-test
  (testing "it returns when values are valid"
    (is (no-multi-ifi? {"mbox" "mailto:milt@yetanalytics.com"})))
  (testing "it returns nil when the values are invalid"
    (is (not (no-multi-ifi? {"foo" "bar"
                             "mbox" "mailto:milt@yetanalytics.com"
                             "account" {"homePage" "http://www.foo.com"
                                        "name" "foobar"}})))))

(deftest ifi-present-test
  (testing "returns when ifi is present"
    (is (ifi-present? {"mbox" "mailto:milt@yetanalytics.com"}))
    (is (not (ifi-present? {"foo" "bar"})))))

(deftest no-ifi-test
  (testing "returns when no ifi is present"
    (is (no-ifi? {}))
    (is (not (no-ifi? {"mbox" "mailto:milt@yetanalytics.com"})))))

(deftest has-members-test
  (testing "returns when members are present"
    (is (has-members? {"member" [{}]}))
    (is (not (has-members? {})))
    (is (not (has-members? {"member" nil})))))

(deftest two-members-test
  "two members?"
  (is (two-members? [{} {}]))
  (is (not (two-members? [{}]))))

(deftest interaction-component-predicates-test
  (testing
   "returns when unique-ids are present"
    (is (unique-ids? [{"id" "1"} {"id" "2"}]))
    (is (not (unique-ids? [{"id" "1"} {"id" "1"}]))))
  (testing
   "returns when valid component keys are present"
    (is (valid-component-keys? {"interactionType" "performance"
                                "steps" "foo"}))
    (is (not (valid-component-keys? {"interactionType" "choice"
                                     "steps" "foo"})))))

(deftest context-predicates-test
  (testing
   "valid-revision?"
    (testing
     "when the statement has a context revision"
      ;;explicit activity
      (is (valid-revision? {"object" {"objectType" "Activity"}
                            "context" {"revision" "foo"}}))

      ;;implicit
      (is (valid-revision? {"object" {}
                            "context" {"revision" "foo"}}))

      (is (not (valid-revision? {"object" {"objectType" "Group"}
                                 "context" {"revision" "foo"}}))))

    (testing
     "when the statement has no context revision"
      (is (valid-revision? {}))))

  (testing
   "valid-platform?"
    (testing
     "when the statement has a context platform"
      ;;explicit activity
      (is (valid-platform? {"object" {"objectType" "Activity"}
                            "context" {"platform" "foo"}}))

      ;;implicit
      (is (valid-platform? {"object" {}
                            "context" {"platform" "foo"}}))
      (is (not (valid-platform? {"object" {"objectType" "Group"}
                                 "context" {"platform" "foo"}}))))

    (testing
     "when the statement has no context platform"
      (is (valid-platform? {})))))

(deftest score-predicates-test
  (testing
   "returns when 'max' is the larger value in the map"
    (is (valid-score?
         {"raw" 5
          "max" 10}))
    (is (not (valid-score?
              {"raw" 10
               "max" 5}))))
  (testing
   "returns when raw is the larger value in the map"
    (is (valid-score?
         {"raw" 5
          "min" 0}))
    (is (not (valid-score?
              {"raw" 1
               "min" 5}))))
  (testing
   "returns when min is the smaller value in the map"
    (is (valid-score?
         {"min" 5
          "max" 10}))
    (is (not (valid-score?
              {"min" 10
               "max" 5}))))
  (testing
      "returns when scaled is between -1.0 and 1.0."
    (is (valid-score?
         {"scaled" 0.4}))
    (is (not (valid-score?
              {"scaled" 1.1})))))

(deftest statement-predicates
  (testing
   "returns when the object is valid or void"
    (is (valid-void? {"verb" {"id" "http://adlnet.gov/expapi/verbs/voided"}
                      "object" {"objectType" "StatementRef"}}))
    (is (valid-void? {}))
    (is (not (valid-void? {"verb" {"id" "http://adlnet.gov/expapi/verbs/voided"}
                           "object" {"objectType" "Activity"}})))))

(deftest utils-test
  (testing
   "re-pred"
    (let [re (atom #"^[A-Z]$")]
      (is ((re-pred @re) "A"))
      (is (not ((re-pred @re) "a"))))))
