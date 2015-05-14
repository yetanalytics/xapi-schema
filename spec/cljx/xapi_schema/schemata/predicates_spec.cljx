(ns xapi-schema.schemata.predicates-spec
  #+cljs (:require-macros [speclj.core :refer [describe context with it should should= should-not run-specs pending]])
  #+cljs (:require [speclj.core]
                   [xapi-schema.schemata.predicates :refer [ifi-count
                                                            no-multi-ifi?
                                                            ifi-present?
                                                            no-ifi?
                                                            unique-ids?
                                                            valid-component-keys?
                                                            valid-revision?
                                                            valid-platform?
                                                            valid-context-pred
                                                            regex-pred
                                                            no-multi-ifi-pred
                                                            one-ifi-required-pred
                                                            AgentValidations
                                                            GroupValidations
                                                            ActivityValidations
                                                            InteractionComponentsValidations
                                                            DefinitionValidations
                                                            StatementReferenceValidations
                                                            ScoreValidations
                                                            StatementValidations
                                                            SubStatementValidations]]
                   [schema.core :as s
                    :include-macros true])
  #+clj (:require [speclj.core :refer :all]
                  [schema.core :as s]
                  [xapi-schema.schemata.predicates :refer :all]))

(describe "ifi predicates"
          (describe "ifi-count"
                    (it "returns the number of IFI-keys in a map"
                        (should= 0 (ifi-count {}))
                        (should= 1 (ifi-count {"foo" "bar"
                                               "mbox" "mailto:milt@yetanalytics.com"}))
                        (should= 2 (ifi-count {"foo" "bar"
                                               "mbox" "mailto:milt@yetanalytics.com"
                                               "account" {"homePage" "http://www.foo.com"
                                                          "name" "foobar"}}))))

          (describe "no-multi-ifi?"
                    (it "returns false if there is more than one IFI"
                        (should (no-multi-ifi? {"mbox" "mailto:milt@yetanalytics.com"}))
                        (should-not (no-multi-ifi? {"foo" "bar"
                                                    "mbox" "mailto:milt@yetanalytics.com"
                                                    "account" {"homePage" "http://www.foo.com"
                                                               "name" "foobar"}}))))

          (describe "ifi-present?"
                    (it "returns true if there is an ifi present"
                        (should (ifi-present? {"mbox" "mailto:milt@yetanalytics.com"}))
                        (should-not (ifi-present? {"foo" "bar"}))))

          (describe "no-ifi?"
                    (it "returns true if there are no ifi keys"
                        (should (no-ifi? {}))
                        (should-not (no-ifi? {"mbox" "mailto:milt@yetanalytics.com"})))))

(describe "interaction component predicates"
          (describe "unique-ids?"
                    (it "Validates that the ID string values in a list of maps are unique."
                        (should (unique-ids? [{"id" "1"} {"id" "2"}]))
                        (should-not (unique-ids? [{"id" "1"} {"id" "1"}]))))
          (describe "valid-component-keys?"
                    (it "ensures that an interaction component's keys are valid for the given interactionType"
                        (should (valid-component-keys? {"interactionType" "performance"
                                                        "steps" "foo"}))
                        (should-not (valid-component-keys? {"interactionType" "choice"
                                                            "steps" "foo"})))))
(describe "context predicates"
          (describe "valid-revision?"
                    (context "when the statement has a context revision"
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

                    (context "when the statement has no context revision"
                             (it "returns true no matter what"
                                 (should (valid-revision? {})))))

          (describe "valid-platform?"
                    (context "when the statement has a context platform"
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

                    (context "when the statement has no context platform"
                             (it "returns true no matter what"
                                 (should (valid-platform? {}))))))

(describe "predicate schemata, builders"
          (describe "valid-context-pred"
                    (context "with valid structure for revision and/or platform"
                             (it "is satisfied"
                                 (should-not (s/check valid-context-pred {"object" {"objectType" "Activity"}
                                                                          "context" {"revision" "whatever"
                                                                                     "platform" "whatever"}}))))
                    (context "with invalid structure for revision and/or plaform"
                             (it "is not satisfied"
                                 (should (s/check valid-context-pred {"object" {"objectType" "StatementRef"}
                                                                      "context" {"revision" "whatever"
                                                                                 "platform" "whatever"}})))))
          (describe "regex-pred"
                    (context "given a regex and a message"
                             (with pred (regex-pred #"[A-Z]" "A capital letter"))
                             (it "creates a regex predicate"
                                 (should-not (s/check @pred "A"))
                                 (should (s/check @pred "a")))))
          (describe "no-multi-ifi-pred"
                    (it "checks that there are not multiple IFIs"
                        (should-not (or (s/check no-multi-ifi-pred {})
                                        (s/check no-multi-ifi-pred {"mbox" "mailto:milt@yetanalytics.com"})))
                        (should (s/check no-multi-ifi-pred  {"foo" "bar"
                                                             "mbox" "mailto:milt@yetanalytics.com"
                                                             "account" {"homePage" "http://www.foo.com"
                                                                        "name" "foobar"}}))))
          (describe "one-ifi-required-pred"
                    (it "checks that there is one and only one IFI"
                        (should-not (s/check one-ifi-required-pred {"mbox" "mailto:milt@yetanalytics.com"}))
                        (should (and (s/check one-ifi-required-pred {})
                                     (s/check one-ifi-required-pred  {"foo" "bar"
                                                                      "mbox" "mailto:milt@yetanalytics.com"
                                                                      "account" {"homePage" "http://www.foo.com"
                                                                                 "name" "foobar"}}))))))
(describe "validation predicate schemata"
          (describe "AgentValidations"
                    (it "verifies that the agent has one and only one IFI"
                        (should-not (s/check AgentValidations
                                             {"mbox" "mailto:milt@yetanalytics.com"}))
                        (should (and (s/check AgentValidations
                                              {"name" "Milt"})
                                     (s/check AgentValidations
                                              {"foo" "bar"
                                               "mbox" "mailto:milt@yetanalytics.com"
                                               "account" {"homePage" "http://www.foo.com"
                                                          "name" "foobar"}})))))
          (describe "GroupValidations"
                    (context "Identified Group"
                             (it "checks to make sure there is only one IFI"
                                 (should-not (s/check GroupValidations
                                                      {"mbox" "mailt:devteam@yetanalytics.com"}))
                                 (should (s/check GroupValidations
                                                  {"mbox" "mailt:devteam@yetanalytics.com"
                                                   "openid" "http://foo.bar"}))))
                    (context "Anonymous Group"
                             (it "ensures the group has a member key"
                                 (should-not (s/check GroupValidations
                                                      {"member" []}))
                                 (should (s/check GroupValidations
                                                  {"name" "somegroup"})))))
          (describe "ActivityValidations"
                    (it "ensures the activity has an ID"
                        (should-not (s/check ActivityValidations
                                             {"id" "http://example.com"}))
                        (should (s/check ActivityValidations
                                         {})))
                    (it "ensures the activity id is valid"
                        (should (s/check ActivityValidations
                                         {"id" "foo"}))))
          (describe "InteractionComponentsValidations"
                    (it "ensures that interaction components have unique ids"
                        (should-not (s/check InteractionComponentsValidations
                                             [{"id" "1"} {"id" "2"}]))
                        (should (s/check InteractionComponentsValidations
                                         [{"id" "1"} {"id" "1"}]))))
          (describe "DefinitionValidations"
                    (it "ensures that interaction component list keys are valid"
                        (should-not (s/check DefinitionValidations
                                             {"interactionType" "performance"
                                              "steps" "foo"}))
                        (should (s/check DefinitionValidations
                                         {"interactionType" "choice"
                                          "steps" "foo"}))))
          (describe "StatementReferenceValidations"
                    (it "ensures that statement reference ID is present and is a valid UUID"
                        (should-not (s/check StatementReferenceValidations
                                             {"id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}))
                        (should (s/check StatementReferenceValidations
                                         {}))
                        (should (s/check StatementReferenceValidations
                                         {"id" "foobar"}))))
          (describe "ScoreValidations"
                    (context "with a valid score"
                             (it "is satisfied"
                                 (should-not (s/check ScoreValidations
                                                      {"max" 10
                                                       "min" 1
                                                       "raw" 5}))))
                    (context "with an invalid score"
                             (it "returns an error"
                                 (should (s/check ScoreValidations
                                                  {"raw" 10 ;; raw higher than max
                                                   "max" 1}))
                                 (should (s/check ScoreValidations
                                                  {"min" 5 ;; raw lower than min
                                                   "raw" 1}))
                                 (should (s/check ScoreValidations
                                                  {"min" 10
                                                   "max" 1})))))
          (describe "StatementValidations"
                    (it "validates statement context (platform and registration) structure"
                        (should-not (s/check StatementValidations
                                             {"object" {"objectType" "Activity"}
                                              "context" {"registration" "whatever"
                                                         "platform" "whatever"}}))
                        (should (s/check StatementValidations
                                         {"object" {"objectType" "Agent"}
                                          "context" {"registration" "whatever"
                                                     "platform" "whatever"}}))))
          (describe "SubstatementValidations"
                    (with mock-substatement {"object" {"objectType" "Activity"}
                                             "context" {"registration" "whatever"
                                                        "platform" "whatever"}})
                    (it "validates statement context (platform and registration) structure"
                        (should-not (s/check SubStatementValidations
                                             @mock-substatement))
                        (should (s/check SubStatementValidations
                                         (assoc-in @mock-substatement ["object" "objectType"] "Agent"))))
                    (it "does not allow the id, stored, version or authority keys"
                        (should (s/check SubStatementValidations
                                         (merge
                                          @mock-substatement
                                          {"id" "whatever"})))))
          (describe "OAuthconsumervalidations"
                    (it ""))
          (describe "Authoritygroupvalidations"
                    (it ""))
          )
