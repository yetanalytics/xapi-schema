(ns xapi-schema.spec-test
  (:require [clojure.test :refer [deftest is testing] :include-macros true]
            [clojure.spec.alpha :as s :include-macros true]
            [xapi-schema.spec :as xs :include-macros true]
            [xapi-schema.support.spec :refer [should-satisfy
                                              should-not-satisfy
                                              should-satisfy+
                                              key-should-satisfy+]]
            [xapi-schema.support.data :as d :refer [simple-statement
                                                    long-statement]]))

(deftest double-conformer-test
  (testing "conforms to double"
    (is (= 1.0
           (s/conform xs/double-conformer 1))))
  (testing "unform is a no-op"
    (is (= 1.0
           (->> 1
                (s/conform xs/double-conformer)
                (s/unform xs/double-conformer))))))

(deftest conform-unform-test
  (is (= long-statement
         (s/unform ::xs/statement (s/conform ::xs/statement long-statement)))))

(deftest conform-ns-map-test
  (is  (= (name (xs/conform-ns-map "foo/bar" []))
          "invalid")))

(deftest language-tag-test
  (testing "is a valid RFC 5646 Language Tag"
    (should-satisfy+ ::xs/language-tag
                     "en-US"
                     :bad
                     "not a tag!")))

(deftest language-map-test
  (testing "has LanguageTags for keys"
    (should-satisfy+ ::xs/language-map
                     {"en-US" "foo"}
                     {"es" "hola mundo"}
                     {"zh-cmn" "你好世界"}
                     {} ; empty lang maps are allowed by spec
                     :bad
                     {"hey there" "foo"}
                     {"en" 2})))

(deftest iri-test
  (testing "must be a valid url with scheme"
    (should-satisfy+ ::xs/iri
                     "http://foo.com"
                     :bad
                     "foo.com")))

(deftest mail-to-iri-test
  (testing "must be a valid foaf mbox"
    (should-satisfy+ ::xs/mailto-iri
                     "mailto:milt@yetanalytics.com"
                     :bad
                     "mailto:user%@example.com"
                     "milt@yetanalytics.com")))

(deftest irl-test
  (testing "must be a valid URL"
    (should-satisfy+ ::xs/irl
                     "http://foo.com"
                     :bad
                     "not an IRL")))

(deftest extensions-test
  (testing "is a map with IRI keys"
    (should-satisfy+ ::xs/extensions
                     {"http://www.foo.bar" {"arbitrary" "data"}}
                     :bad
                     {"foo.bar" {"arbitrary" "data"}})))

(deftest open-id-test
  (testing "is a valid URL"
    (should-satisfy+ ::xs/openid
                     "http://foo.bar/baz"
                     :bad
                     "some other crap")))

(deftest uuid-test
  (testing "is a valid v1-8 UUID"
    (should-satisfy+ ::xs/uuid
                     "f47ac10b-58cc-4372-a567-0e02b2c3d479"
                     "12345678-1234-1234-1234-123456789012"
                     "017b4f9f-2a7e-84f1-80e9-7b788a5baba4"
                     :bad
                     ;; 9 is not a valid version number
                     "12345678-1234-9234-1234-123456789012")))

(deftest timestamp-test
  (testing "is a valid ISO 8601 DateTime"
    (should-satisfy+ ::xs/timestamp
                     "2014-09-10T14:12:05Z"
                     "2014-10-31T14:12:05Z" ; october 31
                     "2015-06-30T23:59:60Z" ; leap second
                     "2020-02-29T01:01:01Z" ; leap year
                     "2000-02-29T01:01:01Z" ; 2000 is a leap year
                     :bad
                     "09-10-2014T14:12:00+500"
                     "2014-09-32T14:12:05Z" ; september 32
                     "2014-09-31T14:12:05Z" ; september 31
                     "2014-10-32T14:12:05Z" ; october 32
                     "2014-09-12T03:47:40"  ; no time zone
                     "2021-02-30T01:01:01Z" ; february 30
                     "2021-02-29T01:01:01Z" ; february 29, non leap year
                     "1900-02-29T01:01:01Z" ; 1900 is not a leap year
                     "2014-09-10T14:12:05.22.33Z")))

(deftest duration-test
  (testing "is a valid ISO 8601 Duration"
    (should-satisfy+ ::xs/duration
                     "P3Y6M4DT12H30M5S"
                     "P3Y6M4DT12H30M5.2S" ;; good fractional
                     :bad
                     "2 hours"
                     "P"
                     "PT"
                     "P3Y6M4DT12H30.1M5S"))) ;; bad fractional


(deftest version-test
  (testing "is a valid xAPI 1.0.X version"
    (should-satisfy+ ::xs/version
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
    (should-satisfy+ ::xs/sha2
                     "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634"
                     :bad
                     123
                     "Q3lxN0R1NQ==")))

(deftest sha1sum-test
  (testing "is a SHA-1 string of 40 hex chars"
    (should-satisfy+
     ::xs/sha1sum
     "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"
     :bad
     "2fd4e1c67a2d28fced849ee1bb76e7391" ;; >40
     "12345"
     "wat"
     2)))

(deftest interaction-component-test
  (testing "must have an ID"
    (should-satisfy+ ::xs/interaction-component
                     {"id" "foo"}
                     :bad
                     {})))

(deftest interaction-components-test
  (testing
   "each component"
    (testing "must have a unique ID"
      (should-satisfy+ ::xs/interaction-components
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
      (should-satisfy+ :activity/definition
                       definition))
    (testing
     "correctResponsesPattern"
      (testing "is an array of strings"
        (key-should-satisfy+
         :activity/definition
         definition-with-interaction-type
         "correctResponsesPattern"
         ["foo" "bar" "baz"]
         :bad
         "foo bar baz")))
    (testing
     "interactionType"
      (testing "is one of true-false, choice, fill-in, long-fill-in, matching,
             performance, sequencing, likert, numeric, other"
        (key-should-satisfy+ :activity/definition
                             definition
                             "interactionType"
                             "true-false" "choice" "fill-in" "long-fill-in" "matching"
                             "performance" "sequencing" "likert" "numeric" "other"
                             :bad "foo")))

    (testing "when the activity is an interaction activity"
      (testing "is satisfied by all interaction types"
        (apply should-satisfy+ :activity/definition
               (vals d/interaction-activity-defs))))))

(deftest activity-test
  (testing
   "id"
    (testing "is required"
      (should-satisfy+ ::xs/activity
                       {"id" "http://foo.com/bar"}
                       :bad
                       {})))
  (testing
   "objectType"
    (testing "must be Activity if present"
      (should-satisfy+ ::xs/activity
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
      (should-satisfy+ ::xs/account
                       {"name" "bob"
                        "homePage" "http://foo.com/bar"}
                       :bad
                       {"homePage" "http://foo.com/bar"})))
  (testing
   "homePage"
    (testing "is required"
      (should-satisfy+ ::xs/account
                       {"name" "bob"
                        "homePage" "http://foo.com/bar"}
                       :bad
                       {"name" "bob"}))))

(deftest agent-test
  (testing "must have one and only one IFI"
    (should-satisfy+ ::xs/agent
                     {"mbox" "mailto:milt@yetanalytics.com"}
                     :bad
                     {}
                     {"mbox" "mailto:milt@yetanalytics.com"
                      "openid" "https://some.site.com/foo"}))
  (testing
   "objectType"
    (testing "must be Agent if provided"
      (key-should-satisfy+ ::xs/agent
                           {"mbox" "mailto:milt@yetanalytics.com"}
                           "objectType"
                           "Agent"
                           :bad
                           "Group"))))

(deftest group-test
  (testing
   "Anonymous Groups"
    (testing "must have a member property"
      (should-satisfy+ ::xs/group
                       {"member" [{"mbox" "mailto:milt@yetanalytics.com"}]
                        "objectType" "Group"}
                       :bad
                       {"objectType" "Group"}
                       {"member" []
                        "objectType" "Group"})))
  (testing
   "Identified Group"
    (testing "must have one or no IFI"
      (should-satisfy+ ::xs/group
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
      (should-satisfy+ ::xs/group
                       {"mbox" "mailto:somegroup@yetanalytics.com"
                        "objectType" "Group"}
                       :bad
                       {"mbox" "mailto:so@yetanalytics.com"}
                       {"mbox" "mailto:somegroup@yetanalytics.com"
                        "objectType" "Agent"}))))

(deftest verb-test
  (testing "id"
    (testing "is required"
      (should-satisfy+ ::xs/verb
                       {"id" "http://foo.bar/baz"}
                       :bad
                       {}))))

(deftest score-test
  (testing "validates score properties"
    (should-satisfy+ :result/score
                     {"raw" 5
                      "min" 1
                      "max" 10}
                     :bad
                     {"raw" 5
                      "max" 1}
                     {"raw" 100
                      "min" 99
                      "max" 1}))
  (testing "can be empty"
    (should-satisfy :result/score {}))
  (testing "can be conformed/unformed, per https://github.com/yetanalytics/xapi-schema/issues/61"
    (is (= {"scaled" 0.5}
           (->> {"scaled" 0.5}
                (s/conform :result/score)
                (s/unform :result/score))))))

(deftest result-test
  (testing "can be empty"
    (should-satisfy ::xs/result {})))

(deftest statement-ref
  (testing
   "id"
    (testing "is required"
      (should-satisfy+ ::xs/statement-ref
                       {"objectType" "StatementRef"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       :bad
                       {"objectType" "StatementRef"})))
  (testing
   "objectType"
    (testing "is required and must be StatementRef"
      (should-satisfy+ ::xs/statement-ref
                       {"objectType" "StatementRef"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       :bad
                       {"objectType" "foo"
                        "id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}
                       {"id" "f47ac10b-58cc-4372-a567-0e02b2c3d479"}))))

(deftest context-activities-test
  (testing "is an array of 1+ activities or single activity"
    (should-satisfy+ ::xs/context-activities
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
    (should-satisfy :context/contextActivities {})))

(deftest context-test
  (testing "can be empty"
    (should-satisfy ::xs/context {}))
  (testing
   "team"
    (testing "must be a group"
      (should-satisfy+ ::xs/context
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
      (should-satisfy+ ::xs/attachment
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
      (should-satisfy+ ::xs/attachment
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
    (should-satisfy+ ::xs/attachments
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
      (should-satisfy+ ::xs/sub-statement
                       minimal-sub-statement
                       :bad
                       (assoc minimal-sub-statement "id" d/uuid-str)
                       (assoc minimal-sub-statement "stored" d/timestamp)
                       (assoc minimal-sub-statement "version" d/version)
                       (assoc minimal-sub-statement "authority" d/agente)))
    (testing
     "actor"
      (testing "is required"
        (should-not-satisfy ::xs/sub-statement (dissoc minimal-sub-statement "actor")))
      (testing "is an agent or group"
        (key-should-satisfy+ ::xs/sub-statement
                             minimal-sub-statement
                             "actor"
                             d/agente
                             d/group
                             d/anon-group
                             :bad
                             "foo"
                             {})))
    (testing
     "verb"
      (testing "is required"
        (should-not-satisfy ::xs/sub-statement (dissoc minimal-sub-statement "verb")))
      (testing "is a Verb"
        (key-should-satisfy+ ::xs/sub-statement
                             minimal-sub-statement
                             "verb"
                             d/verb
                             :bad
                             []
                             {}
                             d/activity
                             d/agente)))
    (testing
     "object"
      (testing "is required"
        (should-not-satisfy ::xs/sub-statement
                            (dissoc minimal-sub-statement "object")))
      (testing "is an Activity, Agent, Group, or StatementRef"
        (key-should-satisfy+ ::xs/sub-statement
                             minimal-sub-statement
                             "object"
                             ;; Activity is the default
                             (dissoc d/activity "objectType")
                             ;; explicit Activity ObjectType
                             d/activity
                             d/agente
                             d/group
                             d/anon-group
                             d/statement-ref
                             :bad
                             []
                             {})))
    (testing
     "objectType"
      (testing "is required"
        (should-not-satisfy ::xs/sub-statement (dissoc minimal-sub-statement "objectType"))))))

(deftest oauth-consumer-test
  (testing "must be identified by account"
    (should-satisfy+
     ::xs/oauth-consumer
     {"account" {"name" "oauth_consumer_x75db"
                 "homePage" "http://example.com/xAPI/OAuth/Token"}}
     :bad
     {"mbox" "mailto:milt@yetanalytics.com"})))

(deftest three-legged-oauth-group-test
  (testing "must be a group with two agents"
    (should-satisfy+
     ::xs/tlo-group
     d/authority-group
     :bad
     (update-in d/authority-group ["member"] (comp vector first))))
  (testing "must have an OAuthConsumer for the first member"
    (should-satisfy+
     ::xs/tlo-group
     d/authority-group
     :bad
     (assoc d/authority-group "member" [d/agente d/agente]))))

(deftest authority-test
  (testing "must be an agent"
    (should-satisfy
     :statement/authority
     d/agente))
  (testing
   "except in the case of three-legged-oauth, when"
    (testing "can be a group with two agents"
      (should-satisfy
       :statement/authority
       d/authority-group))))

(deftest statement-object
  (testing "is an Agent, Group, SubStatement, StatementRef or Activity"
    (should-satisfy+ :statement/object
                     d/agente
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
      (should-not-satisfy ::xs/statement (dissoc long-statement "actor")))
    (testing "is an agent or group"
      (key-should-satisfy+ ::xs/statement
                           long-statement
                           "actor"
                           d/agente
                           d/group
                           d/anon-group
                           :bad
                           "foo"
                           {})))
  (testing
   "verb"
    (testing "is required"
      (should-not-satisfy ::xs/statement (dissoc long-statement "verb")))
    (testing "is a Verb"
      (key-should-satisfy+ ::xs/statement
                           long-statement
                           "verb"
                           d/verb
                           :bad
                           []
                           {}
                           d/activity
                           d/agente)))
  (testing
   "object"
    (testing "is required"
      (should-not-satisfy ::xs/statement
                          (dissoc long-statement "object")))
    (testing "is an Activity, Agent, Group, StatementRef, or sub-statement"
      (key-should-satisfy+ ::xs/statement
                           ;; use one without context so we can swap non-activity objects
                           (dissoc long-statement "context")
                           "object"
                           ;; Activity is the default
                           (dissoc d/activity "objectType")
                           ;; explicit Activity ObjectType
                           d/activity
                           d/agente
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
          (should-satisfy ::xs/statement statement))))
    (testing "when the statement object is not an activity"
      (let [statement d/void-statement]
        (testing "cannot have the platform and revision properties"
          (should-satisfy+ ::xs/statement
                           statement
                           :bad
                           (assoc statement "context" {"platform" "Apple Newton"})
                           (assoc statement "context" {"revision" "whatevs"}))))))

  (testing "is satisfied by all ADL example statements"
    (should-satisfy+ ::xs/statement
                     simple-statement
                     long-statement
                     d/completion-statement
                     d/void-statement))
  (testing "checks for the proper object objectType on voiding statements"
    (should-satisfy+ ::xs/statement
                     d/void-statement
                     :bad
                     {"actor" {"objectType" "Agent"
                               "name" "Example Admin"
                               "mbox" "mailto:admin@example.adlnet.gov"}
                      "verb" {"id" "http://adlnet.gov/expapi/verbs/voided"
                              "display" {"en-US" "voided"}}
                      "object" {"id" "http://example.com/activities/1"}})))

(deftest statements-test
  (testing "generic statememt batch"
    (should-satisfy+ ::xs/statements
                     [simple-statement]
                     [simple-statement long-statement]
                     []))
  (testing "LRS retrieval statement batch"
    (should-satisfy+ ::xs/lrs-statements
                     [d/statement] ; This statement has ID and other required fields
                     :bad
                     [])))
