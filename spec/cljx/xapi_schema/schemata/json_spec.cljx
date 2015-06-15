(ns xapi-schema.schemata.json-spec
  #+cljs (:require-macros [speclj.core :refer [describe context with it should should= should-not run-specs pending]])
  #+cljs (:require [speclj.core]
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
                                                      ContextStatementRef
                                                      ContextActivitiesArray
                                                      ContextActivities
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
                                                           long-statement]])
  #+clj (:require [speclj.core :refer :all]
                  [schema.core :as s]
                  [xapi-schema.schemata.json :refer :all]
                  [xapi-schema.support.data :as d :refer [simple-statement
                                                          long-statement]]
                  [xapi-schema.support.schema :refer [should-satisfy
                                                      should-not-satisfy
                                                      should-satisfy+
                                                      key-should-satisfy+]]))

(describe
 "LanguageTag"
 (it "is a valid RFC 5646 Language Tag"
     (should-satisfy+ LanguageTag
                      "en-US"
                      :bad
                      "not a tag!")))
(describe
 "LanguageMap"
 (it "has LanguageTags for keys"
     (should-satisfy+ LanguageMap
                      {"en-US" "foo"}
                      :bad
                      {"hey there" "foo"})))
(describe
 "IRI"
 (it "must be a valid url with scheme"
     (should-satisfy+ IRI
                      "http://foo.com"
                      :bad
                      "foo.com")))

(describe
 "MailToIRI"
 (it "must be a valid foaf mbox"
     (should-satisfy+ MailToIRI
                      "mailto:milt@yetanalytics.com"
                      :bad
                      "milt@yetanalytics.com")))

(describe
 "IRL"
 (it "must be a valid URL"
     (should-satisfy+ IRL
                      "http://foo.com"
                      :bad
                      "not an IRL")))

(describe
 "Extensions"
 (it "is a map with IRI keys"
     (should-satisfy+ Extensions
                      {"http://www.foo.bar" {"arbitrary" "data"}}
                      :bad
                      {"foo.bar" {"arbitrary" "data"}})))
(describe
 "OpenID"
 (it "is a valid URL"
     (should-satisfy+ OpenID
                      "http://foo.bar/baz"
                      :bad
                      "some other crap")))

(describe
 "UuidId"
 (it "is a valid v4 UUID"
     (should-satisfy+ UuidId
                      "f47ac10b-58cc-4372-a567-0e02b2c3d479"
                      :bad
                      "12345678-1234-1234-1234-123456789012")))
(describe
 "Timestamp"
 (it "is a valid ISO 8601 DateTime"
     (should-satisfy+ Timestamp
                      "2014-09-10T14:12:05Z"
                      "2015-06-30T23:59:60Z" ;; leap second
                      :bad
                      "09-10-2014T14:12:00+500"
                      "2014-09-12T03:47:40" ;; no time zone
                      )))
(describe
 "Duration"
 (it "is a valid ISO 8601 Duration"
     (should-satisfy+ Duration
                      "P3Y6M4DT12H30M5S"
                      :bad
                      "2 hours")))

(describe
 "Version"
 (it "is a valid xAPI 1.0.X version"
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

(describe
 "Sha2"
 (it "is a Base64 encoded string"
     (should-satisfy+ Sha2
                      "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
                      :bad
                      123)))
(describe
 "Sha1Sum"
 (it "is a SHA-1 string of 40 hex chars"
     (should-satisfy+
      Sha1Sum
      "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"
      :bad
      "2fd4e1c67a2d28fced849ee1bb76e7391" ;; >40
      "12345"
      "wat"
      2)))

(describe
 "InteractionComponent"
 (it "must have an ID"
     (should-satisfy+ InteractionComponent
                      {"id" "foo"}
                      :bad
                      {})))

(describe
 "InteractionComponents"
 (context
  "each component"
  (it "must have a unique ID"
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

(describe
 "Definition"
 (with definition d/definition)
 (it "should be satisfied by a valid definition"
     (should-satisfy+ Definition
                      @definition))
 (context
  "correctResponsesPattern"
  (it "is an array of strings"
      (key-should-satisfy+
       Definition @definition
       "correctResponsesPattern"
       ["foo" "bar" "baz"]
       :bad
       "foo bar baz")))
 (context
  "interactionType"
  (it "is one of true-false, choice, fill-in, long-fill-in, matching,
       performance, sequencing, likert, numeric, other"
      (key-should-satisfy+ Definition @definition
                           "interactionType"
                           "true-false" "choice" "fill-in" "long-fill-in" "matching"
                           "performance" "sequencing" "likert" "numeric" "other"
                           :bad "foo")))

 (context "when the activity is an interaction activity"
          (it "is satisfied by all interaction types"
              (apply should-satisfy+ Definition
                     (vals d/interaction-activity-defs)))))

(describe
 "Activity"
 (context
  "id"
  (it "is required"
      (should-satisfy+ Activity
                       {"id" "http://foo.com/bar"}
                       :bad
                       {})))
 (context
  "objectType"
  (it "must be Activity if present"
      (should-satisfy+ Activity
                       {"id" "http://foo.com/bar"}
                       {"id" "http://foo.com/bar"
                        "objectType" "Activity"}
                       :bad
                       {"id" "http://foo.com/bar"
                        "objectType" "foo"}))))

(describe
 "Account"
 (context
  "name"
  (it "is required"
      (should-satisfy+ Account
                       {"name" "bob"
                        "homePage" "http://foo.com/bar"}
                       :bad
                       {"homePage" "http://foo.com/bar"})))
 (context
  "homePage"
  (it "is required"
      (should-satisfy+ Account
                       {"name" "bob"
                        "homePage" "http://foo.com/bar"}
                       :bad
                       {"name" "bob"}))))

(describe
 "Agent"
 (it "must have one and only one IFI"
     (should-satisfy+ Agent
                      {"mbox" "mailto:milt@yetanalytics.com"}
                      :bad
                      {}
                      {"mbox" "mailto:milt@yetanalytics.com"
                       "openid" "https://some.site.com/foo"}))
 (context
  "objectType"
  (it "must be Agent if provided"
      (key-should-satisfy+ Agent
                           {"mbox" "mailto:milt@yetanalytics.com"}
                           "objectType"
                           "Agent"
                           :bad
                           "Group"))))

(describe
 "Group"
 (context
  "Anonymous Groups"
  (it "must have a member property"
      (should-satisfy+ Group
                       {"member" [{"mbox" "mailto:milt@yetanalytics.com"}]}
                       :bad
                       {}
                       {"member" []})))
 (context
  "Identified Group"
  (it "must have one or no IFI"
      (should-satisfy+ Group
                       {"mbox" "mailto:milt@yetanalytics.com"}
                       :bad
                       {}
                       {"mbox" "mailto:milt@yetanalytics.com"
                        "openid" "https://some.site.com/foo"})))
 (context
  "objectType"
  (it "must be Group if provided"
      (key-should-satisfy+ Group
                           {"mbox" "mailto:milt@yetanalytics.com"}
                           "objectType"
                           "Group"
                           :bad
                           "Agent"))))

(describe
 "Verb"
 (context "id"
          (it "is required"
              (should-satisfy+ Verb
                               {"id" "http://foo.bar/baz"}
                               :bad
                               {}))))

(describe
 "Score"
 (it "validates score properties"
     (should-satisfy+ Score
                      {"raw" 5
                       "min" 1
                       "max" 10}
                      :bad
                      {"raw" 5
                       "max" 1}))
 (it "can be empty"
     (should-satisfy Score {})))

(describe
 "Result"
 (it "can be empty"
     (should-satisfy Result {})))

(describe
 "StatementRef"
 (context
  "id"
  (it "is required"
      (should-satisfy+ StatementRef
                       {"objectType" "StatementRef"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       :bad
                       {"objectType" "StatementRef"})))
 (context
  "objectType"
  (it "is required and must be StatementRef"
      (should-satisfy+ StatementRef
                       {"objectType" "StatementRef"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       :bad
                       {"objectType" "foo"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       {"id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}))))

(describe
 "ContextStatementRef"
 (context
  "objectType"
  (it "is not required but must be StatementRef"
      (should-satisfy+ ContextStatementRef
                       {"objectType" "StatementRef"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       {"id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       :bad
                       {"objectType" "foo"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}))))

(describe
 "ContextActivitiesArray"
 (it "is an array of activities"
     (should-satisfy+ ContextActivitiesArray
                      [{"id" "http://foo.bar/baz"
                        "objectType" "Activity"}]
                      [{"id" "http://foo.bar/baz"
                        "objectType" "Activity"}
                       {"id" "http://foo.bar/biz"
                        "objectType" "Activity"}]
                      :bad
                      []
                      ["foo"])))

(describe
 "ContextActivities"
 (it "can be empty"
     (should-satisfy ContextActivities {})))

(describe
 "Context"
 (it "can be empty"
     (should-satisfy Context {})))

(describe
 "Attachment"
 (context
  "usageType, display, contentType, length, sha2"
  (it "are required"
      (should-satisfy+ Attachment
                       {"usageType" "http://foo.bar/baz"
                        "display" {"en-US" "foo"}
                        "contentType" "application/json"
                        "length" 1024
                        "sha2" "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"}
                       :bad
                       {}
                       {"usageType" "http://foo.bar/baz"}))))

(describe
 "UrlAttachment"
 (context
  "usageType, display, contentType, length, sha2, fileUrl"
  (it "are required"
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

(describe
 "Attachments"
 (it "is an array of at least one attachment"
     (should-satisfy+ Attachments
                      [{"usageType" "http://foo.bar/baz"
                        "display" {"en-US" "foo"}
                        "contentType" "application/json"
                        "length" 1024
                        "sha2" "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"}]
                      :bad
                      []
                      {})))

(describe
 "SubStatement"
 (with minimal-sub-statement
       d/sub-statement)
 (it "must not have the id, stored, version, or authority properties"
     (should-satisfy+ SubStatement
                      @minimal-sub-statement
                      :bad
                      (assoc @minimal-sub-statement "id" d/uuid)
                      (assoc @minimal-sub-statement "stored" d/timestamp)
                      (assoc @minimal-sub-statement "version" d/version)
                      (assoc @minimal-sub-statement "authority" d/agent)))
 (context
  "actor"
  (it "is required"
      (should-not-satisfy SubStatement (dissoc @minimal-sub-statement "actor")))
  (it "is an agent or group"
      (key-should-satisfy+ SubStatement
                           @minimal-sub-statement
                           "actor"
                           d/agent
                           d/group
                           d/anon-group
                           :bad
                           "foo"
                           {})))
 (context
  "verb"
  (it "is required"
      (should-not-satisfy SubStatement (dissoc @minimal-sub-statement "verb")))
  (it "is a Verb"
      (key-should-satisfy+ SubStatement
                           @minimal-sub-statement
                           "verb"
                           d/verb
                           :bad
                           []
                           {}
                           d/activity
                           d/agent)))
 (context
  "object"
  (it "is required"
      (should-not-satisfy SubStatement
                          (dissoc @minimal-sub-statement "object")))
  (it "is an Activity, Agent, Group, or StatementRef"
      (key-should-satisfy+ SubStatement
                           @minimal-sub-statement
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
 (context
  "objectType"
  (it "is required"
      (should-not-satisfy SubStatement (dissoc @minimal-sub-statement "objectType")))))

(describe
 "OAuthConsumer"
 (it "must be identified by account"
     (should-satisfy+
      OAuthConsumer
      {"account" {"name" "oauth_consumer_x75db"
                  "homePage" "http://example.com/xAPI/OAuth/Token"}}
      :bad
      {"mbox" "mailto:milt@yetanalytics.com"})))

(describe
 "ThreeLeggedOAuthGroup"
 (it "must be a group with two agents"
     (should-satisfy+
      ThreeLeggedOAuthGroup
      d/authority-group
      :bad
      (update-in d/authority-group ["member"] (comp vector first))))
 (it "must have an OAuthConsumer for the first member"
     (should-satisfy+
      ThreeLeggedOAuthGroup
      d/authority-group
      :bad
      (assoc d/authority-group "member" [d/agent d/agent]))))

(describe
 "Authority"
 (it "must be an agent"
     (should-satisfy
      Authority
      d/agent))
 (context
  "except in the case of three-legged-oauth, when"
  (it "can be a group with two agents"
      (should-satisfy
       Authority
       d/authority-group))))

(describe
 "StatementObject"
 (it "is an Agent, Group, SubStatement, StatementRef or Activity"
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
                      d/verb
                      )))

(describe
 "Statement"
 (context
  "actor"
  (it "is required"
      (should-not-satisfy Statement (dissoc long-statement "actor")))
  (it "is an agent or group"
      (key-should-satisfy+ Statement
                           long-statement
                           "actor"
                           d/agent
                           d/group
                           d/anon-group
                           :bad
                           "foo"
                           {})))
 (context
  "verb"
  (it "is required"
      (should-not-satisfy Statement (dissoc long-statement "verb")))
  (it "is a Verb"
      (key-should-satisfy+ Statement
                           long-statement
                           "verb"
                           d/verb
                           :bad
                           []
                           {}
                           d/activity
                           d/agent)))
 (context
  "object"
  (it "is required"
      (should-not-satisfy Statement
                          (dissoc long-statement "object")))
  (it "is an Activity, Agent, Group, StatementRef, or sub-statement"
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
 (context
  "context"
  (context "when the statement object is an activity"
           (with statement (assoc-in long-statement ["context" "revision"] "whatevs")) ;; has revision and platform
           (it "can have the platform and revision properties"
               (should-satisfy Statement @statement)))
  (context "when the statement object is not an activity"
           (with statement d/void-statement)
           (it "cannot have the platform and revision properties"
               (should-satisfy+ Statement
                                @statement
                                :bad
                                (assoc @statement "context" {"platform" "Apple Newton"})
                                (assoc @statement "context" {"revision" "whatevs"})))))

 (it "is satisfied by all ADL example statements"
     (should-satisfy+ Statement
                      simple-statement
                      long-statement
                      d/completion-statement
                      d/void-statement))
 (it "checks for the proper object objectType on voiding statements"
     (should-satisfy+ Statement
                      d/void-statement
                      :bad
                      {"actor" {"objectType" "Agent"
                                "name" "Example Admin"
                                "mbox" "mailto:admin@example.adlnet.gov"}
                       "verb" {"id" "http://adlnet.gov/expapi/verbs/voided"
                               "display" {"en-US" "voided"}}
                       "object" {"id" "http://example.com/activities/1"}})))
