(ns xapi-schema.schemata.json-test
  (:require #?@(:cljs [[cljs.test :refer-macros [deftest is testing run-tests]]
                        [xapi-schema.schemata.json :refer [LanguageTag
                                                           LanguageMap
                                                           IRI
                                                           MailToIRI
                                                           IRL
                                                           Extensions
                                                           OpenID
                                                           UuidId
                                                           Timestamp
                                                           Duration
                                                           Version
                                                           Sha2
                                                           Sha1Sum
                                                           InteractionComponent
                                                           InteractionComponents
                                                           Definition
                                                           Activity
                                                           Account
                                                           Agent
                                                           Group
                                                           Actor
                                                           Verb
                                                           Score
                                                           Result
                                                           StatementRef
                                                           ContextActivitiesArray
                                                           ContextActivities
                                                           ContextActivitiesMap
                                                           Context
                                                           Attachment
                                                           UrlAttachment
                                                           Attachments
                                                           SubStatement
                                                           OAuthConsumer
                                                           ThreeLeggedOAuthGroup
                                                           Authority
                                                           StatementObject
                                                           Statement
                                                           Statements]]
                        [schema.core :as s
                         :include-macros true]
                        [xapi-schema.support.schema :refer [should-satisfy
                                                            should-not-satisfy
                                                            should-satisfy+
                                                            key-should-satisfy+]]
                        [xapi-schema.support.data :as d :refer [simple-statement
                                                                long-statement]]])
            #?@(:clj [[clojure.test :refer :all]
                       [schema.core :as s]
                       [xapi-schema.schemata.json :refer :all]
                       [xapi-schema.support.data :as d :refer [simple-statement
                                                               long-statement]]
                       [xapi-schema.support.schema :refer :all]])))

(deftest language-tag-test
  (testing "is a valid RFC 5646 Language Tag"
    (should-satisfy+ LanguageTag
                     "en-US"
                     :bad
                     "not a tag!")))
(deftest language-map-test
  (testing "has LanguageTags for keys"
    (should-satisfy+ LanguageMap
                     {"en-US" "foo"}
                     :bad
                     {"hey there" "foo"})))
(deftest iri-test
  (testing "must be a valid url with scheme"
    (should-satisfy+ IRI
                     "http://foo.com"
                     :bad
                     "foo.com")))

(deftest mail-to-iri-test
  (testing "must be a valid foaf mbox"
    (should-satisfy+ MailToIRI
                     "mailto:milt@yetanalytics.com"
                     :bad
                     "mailto:user%@example.com"
                     "milt@yetanalytics.com")))

(deftest irl-test
  (testing "must be a valid URL"
    (should-satisfy+ IRL
                     "http://foo.com"
                     :bad
                     "not an IRL")))

(deftest extensions-test
  (testing "is a map with IRI keys"
    (should-satisfy+ Extensions
                     {"http://www.foo.bar" {"arbitrary" "data"}}
                     :bad
                     {"foo.bar" {"arbitrary" "data"}})))
(deftest open-id-test
  (testing "is a valid URL"
    (should-satisfy+ OpenID
                     "http://foo.bar/baz"
                     :bad
                     "some other crap")))

(deftest uuid-test
  (testing "is a valid v4 UUID"
    (should-satisfy+ UuidId
                     "f47ac10b-58cc-4372-a567-0e02b2c3d479"
                     :bad
                     "12345678-1234-1234-1234-123456789012")))
(deftest timestamp-test
  (testing "is a valid ISO 8601 DateTime"
    (should-satisfy+ Timestamp
                     "2014-09-10T14:12:05Z"
                     "2015-06-30T23:59:60Z" ;; leap second
                     :bad
                     "09-10-2014T14:12:00+500"
                     "2014-09-12T03:47:40" ;; no time zone
)))
(deftest duration-test
  (testing "is a valid ISO 8601 Duration"
    (should-satisfy+ Duration
                     "P3Y6M4DT12H30M5S"
                     "P3Y6M4DT12H30M5.2S" ;; good fractional
                     :bad
                     "2 hours"
                     "P"
                     "PT"
                     "P3Y6M4DT12H30.1M5S" ;; bad fractional
)))

(deftest version-test
  (testing "is a valid xAPI 1.0.X version"
    (should-satisfy+ Version
                     "1.0.0"
                     "1.0.1"
                     "1.0.2"
                     "1.0.3"
                     "1.0.3-rc1"
                     "1.0.0-alpha"
                     "1.0.0-alpha.1"
                     "1.0.0-0.3.7"
                     "1.0.0-x.7.z.92"
                     :bad
                     "0.9.5"
                     "1.0." ;; bad semver
                     "1.0.0-.123"
                     "1.0.0-..."
                     "1.0.0-123."
                     "1.0.0-+"
                     "1.0.0-+123"
                     "1.0.0-"
                     "what's going on?")))

(deftest sha2-test
  (testing "is a Base64 encoded string"
    (should-satisfy+ Sha2
                     "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
                     :bad
                     123)))
(deftest sha1sum-test
  (testing "is a SHA-1 string of 40 hex chars"
    (should-satisfy+
     Sha1Sum
     "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"
     :bad
     "2fd4e1c67a2d28fced849ee1bb76e7391" ;; >40
     "12345"
     "wat"
     2)))

(deftest interaction-component-test
  (testing "must have an ID"
    (should-satisfy+ InteractionComponent
                     {"id" "foo"}
                     :bad
                     {})))

(deftest interaction-components-test
  (testing
   "each component"
    (testing "must have a unique ID"
      (should-satisfy+ InteractionComponents
                       [{"id" "1"
                         "description" {"en-US" "foo"}}
                        {"id" "2"
                         "description" {"en-US" "bar"}}]
                       :bad
                       [{"id" "1"
                         "description" {"en-US" "foo"}}
                        {"id" "1"
                         "description" {"en-US" "bar"}}]))))

(deftest definition-test
  (let [definition                       d/definition
        definition-with-interaction-type d/definition-with-interaction-type]
    (testing "should be satisfied by a valid definition"
      (should-satisfy+ Definition
                       definition))
    (testing
     "correctResponsesPattern"
      (testing "is an array of strings"
        (key-should-satisfy+
         Definition definition-with-interaction-type
         "correctResponsesPattern"
         ["foo" "bar" "baz"]
         :bad
         "foo bar baz")))
    (testing
     "interactionType"
      (testing "is one of true-false, choice, fill-in, long-fill-in, matching,
             performance, sequencing, likert, numeric, other"
        (key-should-satisfy+ Definition definition
                             "interactionType"
                             "true-false" "choice" "fill-in" "long-fill-in" "matching"
                             "performance" "sequencing" "likert" "numeric" "other"
                             :bad "foo")))

    (testing "when the activity is an interaction activity"
      (testing "is satisfied by all interaction types"
        (apply should-satisfy+ Definition
               (vals d/interaction-activity-defs))))))

(deftest activity-test
  (testing
   "id"
    (testing "is required"
      (should-satisfy+ Activity
                       {"id" "http://foo.com/bar"}
                       :bad
                       {})))
  (testing
   "objectType"
    (testing "must be Activity if present"
      (should-satisfy+ Activity
                       {"id" "http://foo.com/bar"}
                       {"id" "http://foo.com/bar"
                        "objectType" "Activity"}
                       :bad
                       {"id" "http://foo.com/bar"
                        "objectType" "foo"}))))

(deftest account-test
  (testing
   "name"
    (testing "is required"
      (should-satisfy+ Account
                       {"name" "bob"
                        "homePage" "http://foo.com/bar"}
                       :bad
                       {"homePage" "http://foo.com/bar"})))
  (testing
   "homePage"
    (testing "is required"
      (should-satisfy+ Account
                       {"name" "bob"
                        "homePage" "http://foo.com/bar"}
                       :bad
                       {"name" "bob"}))))

(deftest agent-test
  (testing "must have one and only one IFI"
    (should-satisfy+ Agent
                     {"mbox" "mailto:milt@yetanalytics.com"}
                     :bad
                     {}
                     {"mbox" "mailto:milt@yetanalytics.com"
                      "openid" "https://some.site.com/foo"}))
  (testing
   "objectType"
    (testing "must be Agent if provided"
      (key-should-satisfy+ Agent
                           {"mbox" "mailto:milt@yetanalytics.com"}
                           "objectType"
                           "Agent"
                           :bad
                           "Group"))))

(deftest group-test
  (testing
   "Anonymous Groups"
    (testing "must have a member property"
      (should-satisfy+ Group
                       {"member" [{"mbox" "mailto:milt@yetanalytics.com"}]
                        "objectType" "Group"}
                       :bad
                       {"objectType" "Group"}
                       {"member" []
                        "objectType" "Group"})))
  (testing
   "Identified Group"
    (testing "must have one or no IFI"
      (should-satisfy+ Group
                       {"mbox" "mailto:milt@yetanalytics.com"
                        "objectType" "Group"}
                       :bad
                       {"objectType" "Group"}
                       {"mbox" "mailto:milt@yetanalytics.com"
                        "openid" "https://some.site.com/foo"
                        "objectType" "Group"})))
  (testing
   "objectType"
    (testing "must be present and be Group"
      (should-satisfy+ Group
                       {"mbox" "mailto:somegroup@yetanalytics.com"
                        "objectType" "Group"}
                       :bad
                       {"mbox" "mailto:so@yetanalytics.com"}
                       {"mbox" "mailto:somegroup@yetanalytics.com"
                        "objectType" "Agent"}))))

(deftest verb-test
  (testing "id"
    (testing "is required"
      (should-satisfy+ Verb
                       {"id" "http://foo.bar/baz"}
                       :bad
                       {}))))

(deftest score-test
  (testing "validates score properties"
    (should-satisfy+ Score
                     {"raw" 5
                      "min" 1
                      "max" 10}
                     :bad
                     {"raw" 5
                      "max" 1}))
  (testing "can be empty"
    (should-satisfy Score {})))

(deftest result-test
  (testing "can be empty"
    (should-satisfy Result {})))

(deftest statement-ref
  (testing
   "id"
    (testing "is required"
      (should-satisfy+ StatementRef
                       {"objectType" "StatementRef"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       :bad
                       {"objectType" "StatementRef"})))
  (testing
   "objectType"
    (testing "is required and must be StatementRef"
      (should-satisfy+ StatementRef
                       {"objectType" "StatementRef"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       :bad
                       {"objectType" "foo"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       {"id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}))))

(deftest context-activities-test
  (testing "is an array of 1+ activities or single activity"
    (should-satisfy+ ContextActivities
                     [{"id" "http://foo.bar/baz"
                       "objectType" "Activity"}]
                     [{"id" "http://foo.bar/baz"
                       "objectType" "Activity"}
                      {"id" "http://foo.bar/biz"
                       "objectType" "Activity"}]
                     {"id" "http://foo.bar/baz"
                      "objectType" "Activity"}
                     :bad
                     []
                     ["foo"])))

(deftest context-activities-map-test
  (testing "can be empty"
    (should-satisfy ContextActivitiesMap {})))

(deftest context-test
  (testing "can be empty"
    (should-satisfy Context {}))
  (testing
   "team"
    (testing "must be a group"
      (should-satisfy+ Context
                       {"team" {"mbox" "mailto:a@b.com"
                                "objectType" "Group"}}
                       :bad
                       {"team" {"mbox" "mailto:a@b.com"}}
                       {"team" {"mbox" "mailto:a@b.com"
                                "objectType" "Agent"}}))))

(deftest attachment-test
  (testing
   "usageType, display, contentType, length, sha2"
    (testing "are required"
      (should-satisfy+ Attachment
                       {"usageType" "http://foo.bar/baz"
                        "display" {"en-US" "foo"}
                        "contentType" "application/json"
                        "length" 1024
                        "sha2" "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"}
                       :bad
                       {}
                       {"usageType" "http://foo.bar/baz"}))))

(deftest url-attachment-test
  (testing
   "usageType, display, contentType, length, sha2, fileUrl"
    (testing "are required"
      (should-satisfy+ UrlAttachment
                       {"usageType" "http://foo.bar/baz"
                        "display" {"en-US" "foo"}
                        "contentType" "application/json"
                        "length" 1024
                        "sha2" "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
                        "fileUrl" "http://foo.bar/baz"}
                       :bad
                       {}
                       {"usageType" "http://foo.bar/baz"}))))

(deftest attachments-test
  (testing "is an array of at least one attachment"
    (should-satisfy+ Attachments
                     [{"usageType" "http://foo.bar/baz"
                       "display" {"en-US" "foo"}
                       "contentType" "application/json"
                       "length" 1024
                       "sha2" "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"}]
                     :bad
                     []
                     {})))

(deftest sub-statement-test
  (let [minimal-sub-statement d/sub-statement]
    (testing "must not have the id, stored, version, or authority properties"
      (should-satisfy+ SubStatement
                       minimal-sub-statement
                       :bad
                       (assoc minimal-sub-statement "id" d/uuid)
                       (assoc minimal-sub-statement "stored" d/timestamp)
                       (assoc minimal-sub-statement "version" d/version)
                       (assoc minimal-sub-statement "authority" d/agent)))
    (testing
     "actor"
      (testing "is required"
        (should-not-satisfy SubStatement (dissoc minimal-sub-statement "actor")))
      (testing "is an agent or group"
        (key-should-satisfy+ SubStatement
                             minimal-sub-statement
                             "actor"
                             d/agent
                             d/group
                             d/anon-group
                             :bad
                             "foo"
                             {})))
    (testing
     "verb"
      (testing "is required"
        (should-not-satisfy SubStatement (dissoc minimal-sub-statement "verb")))
      (testing "is a Verb"
        (key-should-satisfy+ SubStatement
                             minimal-sub-statement
                             "verb"
                             d/verb
                             :bad
                             []
                             {}
                             d/activity
                             d/agent)))
    (testing
     "object"
      (testing "is required"
        (should-not-satisfy SubStatement
                            (dissoc minimal-sub-statement "object")))
      (testing "is an Activity, Agent, Group, or StatementRef"
        (key-should-satisfy+ SubStatement
                             minimal-sub-statement
                             "object"
                             ;; Activity is the default
                             (dissoc d/activity "objectType")
                             ;; explicit Activity ObjectType
                             d/activity
                             d/agent
                             d/group
                             d/anon-group
                             d/statement-ref
                             :bad
                             []
                             {})))
    (testing
     "objectType"
      (testing "is required"
        (should-not-satisfy SubStatement (dissoc minimal-sub-statement "objectType"))))))

(deftest oauth-consumer-test
  (testing "must be identified by account"
    (should-satisfy+
     OAuthConsumer
     {"account" {"name" "oauth_consumer_x75db"
                 "homePage" "http://example.com/xAPI/OAuth/Token"}}
     :bad
     {"mbox" "mailto:milt@yetanalytics.com"})))

(deftest three-legged-oauth-group-test
  (testing "must be a group with two agents"
    (should-satisfy+
     ThreeLeggedOAuthGroup
     d/authority-group
     :bad
     (update-in d/authority-group ["member"] (comp vector first))))
  (testing "must have an OAuthConsumer for the first member"
    (should-satisfy+
     ThreeLeggedOAuthGroup
     d/authority-group
     :bad
     (assoc d/authority-group "member" [d/agent d/agent]))))

(deftest authority-test
  (testing "must be an agent"
    (should-satisfy
     Authority
     d/agent))
  (testing
   "except in the case of three-legged-oauth, when"
    (testing "can be a group with two agents"
      (should-satisfy
       Authority
       d/authority-group))))

(deftest statement-object
  (testing "is an Agent, Group, SubStatement, StatementRef or Activity"
    (should-satisfy+ StatementObject
                     d/agent
                     d/group
                     d/anon-group
                     d/sub-statement
                     d/statement-ref
                     d/activity
                     (dissoc d/activity "objectType")
                     :bad
                     []
                     {}
                     d/verb)))

(deftest statement-test
  (testing
   "actor"
    (testing "is required"
      (should-not-satisfy Statement (dissoc long-statement "actor")))
    (testing "is an agent or group"
      (key-should-satisfy+ Statement
                           long-statement
                           "actor"
                           d/agent
                           d/group
                           d/anon-group
                           :bad
                           "foo"
                           {})))
  (testing
   "verb"
    (testing "is required"
      (should-not-satisfy Statement (dissoc long-statement "verb")))
    (testing "is a Verb"
      (key-should-satisfy+ Statement
                           long-statement
                           "verb"
                           d/verb
                           :bad
                           []
                           {}
                           d/activity
                           d/agent)))
  (testing
   "object"
    (testing "is required"
      (should-not-satisfy Statement
                          (dissoc long-statement "object")))
    (testing "is an Activity, Agent, Group, StatementRef, or sub-statement"
      (key-should-satisfy+ Statement
                           ;; use one without context so we can swap non-activity objects
                           (dissoc long-statement "context")
                           "object"
                           ;; Activity is the default
                           (dissoc d/activity "objectType")
                           ;; explicit Activity ObjectType
                           d/activity
                           d/agent
                           d/group
                           d/anon-group
                           d/sub-statement
                           :bad
                           []
                           {})))
  (testing
   "context"
    (testing "when the statement object is an activity"
      (let [statement (assoc-in long-statement ["context" "revision"] "whatevs")]
        (testing "can have the platform and revision properties"
          (should-satisfy Statement statement))))
    (testing "when the statement object is not an activity"
      (let [statement d/void-statement]
        (testing "cannot have the platform and revision properties"
          (should-satisfy+ Statement
                           statement
                           :bad
                           (assoc statement "context" {"platform" "Apple Newton"})
                           (assoc statement "context" {"revision" "whatevs"}))))))

  (testing "is satisfied by all ADL example statements"
    (should-satisfy+ Statement
                     simple-statement
                     long-statement
                     d/completion-statement
                     d/void-statement))
  (testing "checks for the proper object objectType on voiding statements"
    (should-satisfy+ Statement
                     d/void-statement
                     :bad
                     {"actor" {"objectType" "Agent"
                               "name" "Example Admin"
                               "mbox" "mailto:admin@example.adlnet.gov"}
                      "verb" {"id" "http://adlnet.gov/expapi/verbs/voided"
                              "display" {"en-US" "voided"}}
                      "object" {"id" "http://example.com/activities/1"}})))
