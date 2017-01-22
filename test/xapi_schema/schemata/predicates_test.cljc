(ns xapi-schema.schemata.predicates-test
  #?@(:cljs [(:require [cljs.test :refer-macros [testing is testing run-tests deftest]]
                       [xapi-schema.schemata.predicates :refer [ifi-count
                                                                no-ifi?
                                                                re-pred
                                                                unique-ids?
                                                                valid-component-keys?
                                                                no-multi-ifi?
                                                                ifi-present?
                                                                has-members?
                                                                score-raw-lte-max
                                                                score-raw-gte-min
                                                                score-min-lt-max
                                                                two-members?
                                                                valid-revision?
                                                                valid-platform?
                                                                valid-void?
                                                                ]]
                       [schema.core :as s
                        :include-macros true]
                       [xapi-schema.support.schema :refer [should-satisfy
                                                           should-not-satisfy
                                                           should-satisfy+]])])
  #?(:clj (:require [clojure.test :refer :all]
                    [schema.core :as s]
                    [xapi-schema.schemata.predicates :refer :all]
                    [xapi-schema.support.schema :refer [should-satisfy
                                                        should-not-satisfy
                                                        should-satisfy+]])))


(deftest predicates-test
 (testing
   "ifi-count"
   (is (= 0 (ifi-count {})))
   (is (= 1 (ifi-count {"foo" "bar"
                        "mbox" "mailto:milt@yetanalytics.com"})))
   (is (= 2 (ifi-count {"foo" "bar"
                        "mbox" "mailto:milt@yetanalytics.com"
                        "account" {"homePage" "http://www.foo.com"
                                   "name" "foobar"}}))))

 (testing
   "no-multi-ifi?"
   (is (true? (no-multi-ifi? {"mbox" "mailto:milt@yetanalytics.com"})))
   (is (false? (no-multi-ifi? {"foo" "bar"
                               "mbox" "mailto:milt@yetanalytics.com"
                               "account" {"homePage" "http://www.foo.com"
                                          "name" "foobar"}}))))

 (testing
   "ifi-present?"
   (is (true? (ifi-present? {"mbox" "mailto:milt@yetanalytics.com"})))
   (is (false? (ifi-present? {"foo" "bar"})))

   (testing
     "no-ifi?"
     (is (true? (no-ifi? {})))
     (is (false? (no-ifi? {"mbox" "mailto:milt@yetanalytics.com"})))))

 (testing
   "group predicates"
   (testing
     "has-members?"
     (is (= '({}) (has-members? {"member" [{}]}))))
   (is (nil? (has-members? {})))
   (is (nil? (has-members? {"member" nil}))))

 (testing
   "two members?"
   (is (two-members? [{}{}]))
   (is (not (two-members? [{}]))))


 (testing
   "interaction component predicates"
   (testing
     "unique-ids?"
     (is (unique-ids? [{"id" "1"} {"id" "2"}]))
     (is (not (unique-ids? [{"id" "1"} {"id" "1"}]))))
   (testing
     "valid-component-keys?"
     (is (valid-component-keys? {"interactionType" "performance"
                                 "steps" "foo"}))
     (is (not (valid-component-keys? {"interactionType" "choice"
                                      "steps" "foo"})))))

 (testing
   "context predicates"
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

 (testing
   "Score Predicates"
   (testing
     "score-raw-lte-max"
     (is (score-raw-lte-max
           {"raw" 5
            "max" 10}))
     (is (not (score-raw-lte-max
                {"raw" 10
                 "max" 5}))))
   (testing
     "score-raw-gte-min"
     (is (score-raw-gte-min
           {"raw" 5
            "min" 0}))
     (is (not (score-raw-gte-min
                {"raw" 1
                 "min" 5}))))
   (testing
     "score-min-lt-max"
     (is (score-min-lt-max
           {"min" 5
            "max" 10}))
     (is (not (score-min-lt-max
                {"min" 10
                 "max" 5})))))

(testing
  "Statement Predicates"
  (testing
    "valid-void?"
    (is (valid-void? {"verb" {"id" "http://adlnet.gov/expapi/verbs/voided"}
                      "object" {"objectType" "StatementRef"}}))
    (is (valid-void? {}))
    (is (not (valid-void? {"verb" {"id" "http://adlnet.gov/expapi/verbs/voided"}
                           "object" {"objectType" "Activity"}})))))

(testing
  "Utils"
  (testing
    "re-pred"
    (let [re (atom #"^[A-Z]$")]
      (is ((re-pred @re) "A"))
      (is (not ((re-pred @re) "a")))))))
