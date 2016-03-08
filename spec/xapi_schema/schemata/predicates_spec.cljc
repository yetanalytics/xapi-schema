(ns xapi-schema.schemata.predicates-spec
  #?@(:cljs [(:require-macros [speclj.core :refer [describe
                                                   context
                                                   with
                                                   it
                                                   should
                                                   should=
                                                   should-not
                                                   run-specs
                                                   pending]])
             (:require [speclj.core]
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
  #?(:clj (:require [speclj.core :refer :all]
                    [schema.core :as s]
                    [xapi-schema.schemata.predicates :refer :all]
                    [xapi-schema.support.schema :refer [should-satisfy
                                                        should-not-satisfy
                                                        should-satisfy+]])))

(describe
 "ifi predicates"
 (describe
  "ifi-count"
  (it "returns the number of IFI-keys in a map"
      (should= 0 (ifi-count {}))
      (should= 1 (ifi-count {"foo" "bar"
                             "mbox" "mailto:milt@yetanalytics.com"}))
      (should= 2 (ifi-count {"foo" "bar"
                             "mbox" "mailto:milt@yetanalytics.com"
                             "account" {"homePage" "http://www.foo.com"
                                        "name" "foobar"}}))))

 (describe
  "no-multi-ifi?"
  (it "returns false if there is more than one IFI"
      (should (no-multi-ifi? {"mbox" "mailto:milt@yetanalytics.com"}))
      (should-not (no-multi-ifi? {"foo" "bar"
                                  "mbox" "mailto:milt@yetanalytics.com"
                                  "account" {"homePage" "http://www.foo.com"
                                             "name" "foobar"}}))))

 (describe
  "ifi-present?"
  (it "returns true if there is an ifi present"
      (should (ifi-present? {"mbox" "mailto:milt@yetanalytics.com"}))
      (should-not (ifi-present? {"foo" "bar"}))))

 (describe
  "no-ifi?"
  (it "returns true if there are no ifi keys"
      (should (no-ifi? {}))
      (should-not (no-ifi? {"mbox" "mailto:milt@yetanalytics.com"})))))

(describe
 "group predicates"
 (describe
  "has-members?"
  (it "is truthy if the group object has members"
      (should (has-members? {"member" [{}]}))
      (should-not (has-members? {}))
      (should-not (has-members? {"member" nil}))))
 (describe
  "two members?"
  (it "is truthy if the members vector has exactly two members"
      (should (two-members? [{}{}]))
      (should-not (two-members? [{}])))))


(describe
 "interaction component predicates"
 (describe
  "unique-ids?"
  (it "Validates that the ID string values in a list of maps are unique."
      (should (unique-ids? [{"id" "1"} {"id" "2"}]))
      (should-not (unique-ids? [{"id" "1"} {"id" "1"}]))))
 (describe
  "valid-component-keys?"
  (it "ensures that an interaction component's keys are valid for the given interactionType"
      (should (valid-component-keys? {"interactionType" "performance"
                                      "steps" "foo"}))
      (should-not (valid-component-keys? {"interactionType" "choice"
                                          "steps" "foo"})))))

(describe
 "context predicates"
 (describe
  "valid-revision?"
  (context
   "when the statement has a context revision"
   (it "returns true if the statement's object is an activity"
       ;;explicit activity
       (should (valid-revision? {"object" {"objectType" "Activity"}
                                 "context" {"revision" "foo"}}))

       ;;implicit
       (should (valid-revision? {"object" {}
                                 "context" {"revision" "foo"}})))
   (it "returns false otherwise"
               (should-not (valid-revision? {"object" {"objectType" "Group"}
                                             "context" {"revision" "foo"}}))))

  (context
   "when the statement has no context revision"
   (it "returns true no matter what"
       (should (valid-revision? {})))))

 (describe
  "valid-platform?"
  (context
   "when the statement has a context platform"
   (it "returns true if the statement's object is an activity"
       ;;explicit activity
       (should (valid-platform? {"object" {"objectType" "Activity"}
                                 "context" {"platform" "foo"}}))

       ;;implicit
       (should (valid-platform? {"object" {}
                                 "context" {"platform" "foo"}})))
   (it "returns false otherwise"
       (should-not (valid-platform? {"object" {"objectType" "Group"}
                                     "context" {"platform" "foo"}}))))

  (context
   "when the statement has no context platform"
   (it "returns true no matter what"
       (should (valid-platform? {}))))))

(describe
 "Score Predicates"
 (describe
  "score-raw-lte-max"
  (it "is truthy if the raw score is less than or equal to max"
      (should (score-raw-lte-max
               {"raw" 5
                "max" 10}))
      (should-not (score-raw-lte-max
                   {"raw" 10
                    "max" 5}))))
 (describe
  "score-raw-gte-min"
  (it "is truthy if the raw score is greater than or equal to min"
      (should (score-raw-gte-min
               {"raw" 5
                "min" 0}))
      (should-not (score-raw-gte-min
                   {"raw" 1
                    "min" 5}))))
 (describe
  "score-min-lt-max"
  (it "is truthy if the min score is less than max"
      (should (score-min-lt-max
               {"min" 5
                "max" 10}))
      (should-not (score-min-lt-max
                   {"min" 10
                    "max" 5})))))

(describe
 "Statement Predicates"
 (describe
  "valid-void?"
  (it "if the statement verb is void it checks for a statement ref object, otherwise true"
      (should (valid-void? {"verb" {"id" "http://adlnet.gov/expapi/verbs/voided"}
                           "object" {"objectType" "StatementRef"}}))
      (should (valid-void? {}))
      (should-not (valid-void? {"verb" {"id" "http://adlnet.gov/expapi/verbs/voided"}
                               "object" {"objectType" "Activity"}})))))

(describe
 "Utils"
 (describe
  "re-pred"
  (xit "takes a regex, and returns a simple predicate fn"
      )))
