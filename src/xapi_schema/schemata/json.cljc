(ns xapi-schema.schemata.json
  (:require
   [xapi-schema.schemata.predicates :refer [agent?
                                            group?
                                            activity?
                                            sub-statement?
                                            statement-ref?
                                            no-object-type?
                                            agent-actor?
                                            activity-object?
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
                                            valid-void?]]
   [xapi-schema.schemata.regex :refer [LanguageTagRegEx
                                       OpenIdRegEx
                                       AbsoluteIRIRegEx
                                       MailToIRIRegEx
                                       UuidRegEx
                                       TimestampRegEx
                                       xAPIVersionRegEx
                                       DurationRegEx
                                       Base64RegEx
                                       Sha1RegEx]]
   [schema.core :as s
    #?@(:cljs [:include-macros true])]
   [schema.experimental.abstract-map :as am
    #?@(:cljs [:include-macros true])]))


;; Scalar schemata

(s/defschema
  LanguageTag
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#52-language-map"
  (s/named
   LanguageTagRegEx
   :valid/ltag))

;; used to designate Ltag keys, so they don't become data
(s/defschema
  LanguageTagKey
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#52-language-map"
  (s/named
   LanguageTagRegEx
   :valid-key/ltag))

(s/defschema
  IRI
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#def-iri"
  (s/named
   AbsoluteIRIRegEx
   :valid/iri))

;; used to designate IRI keys
(s/defschema
  IRIKey
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#def-iri"
  (s/named
   AbsoluteIRIRegEx
   :valid-key/iri))

(s/defschema
  MailToIRI
  "http://xmlns.com/foaf/spec/#term_mbox"
  (s/named
   MailToIRIRegEx
   :valid/mailto-iri))

(s/defschema
  IRL
  (s/named
   AbsoluteIRIRegEx
   :valid/irl))

(s/defschema
  OpenID
  (s/named
   OpenIdRegEx
   :valid/openid))

(s/defschema
  UuidId
  (s/named
   UuidRegEx
   :valid/uuid))

(s/defschema
  Timestamp
  (s/named
   TimestampRegEx
   :valid/timestamp))

(s/defschema
  Duration
  (s/named
   DurationRegEx
   :valid/duration))

(s/defschema
  Version
  (s/named
   xAPIVersionRegEx
   :valid/xapi-version))

(s/defschema
  Sha2
  (s/named
   Base64RegEx
   :valid/sha-2-sum))

(s/defschema
  Sha1Sum
  (s/named
   Sha1RegEx
   :valid/sha-1-sum))


;; Composite schemata

(s/defschema
  Extensions
  {IRIKey s/Any})

(s/defschema
  LanguageMap
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#52-language-map"
  {LanguageTagKey s/Str})

(s/defschema
  InteractionComponent
  {(s/required-key "id") s/Str
   (s/optional-key "description") LanguageMap})

(s/defschema
  InteractionComponents
  (s/constrained [InteractionComponent]
                 unique-ids?
                 :predicates/distinct-ic-ids))

(s/defschema
  Definition
  (s/constrained
   {(s/optional-key "name") LanguageMap
    (s/optional-key "description") LanguageMap
    (s/optional-key "correctResponsesPattern") [s/Str]
    (s/optional-key "interactionType")
    (s/enum "true-false"
            "choice"
            "fill-in"
            "long-fill-in"
            "matching"
            "performance"
            "sequencing"
            "likert"
            "numeric"
            "other")
    (s/optional-key "type") IRI
    (s/optional-key "moreInfo") IRL
    (s/optional-key "choices") InteractionComponents
    (s/optional-key "scale") InteractionComponents
    (s/optional-key "source") InteractionComponents
    (s/optional-key "target") InteractionComponents
    (s/optional-key "steps") InteractionComponents
    (s/optional-key "extensions") Extensions}
   valid-component-keys?
   :predicates/valid-component-keys))

(s/defschema
  AbstractActivity
  {(s/optional-key "id") s/Str
   (s/optional-key "definition") Definition})

(s/defschema
  Activity
  {(s/optional-key "objectType") (s/eq "Activity")
   (s/required-key "id") IRI
   (s/optional-key "definition") Definition})

(s/defschema
  Account
  {(s/required-key "homePage") IRL
   (s/required-key "name") s/Str})


(s/defschema
  Agent
  (-> {(s/optional-key "objectType") (s/eq "Agent")
       (s/optional-key "name") s/Str
       (s/optional-key "mbox") MailToIRI
       (s/optional-key "mbox_sha1sum") Sha1Sum
       (s/optional-key "openid") OpenID
       (s/optional-key "account") Account}
      (s/constrained ifi-present? :predicates/no-ifi)
      (s/constrained no-multi-ifi? :predicates/no-multi-ifi)))

(s/defschema
  Group
  (s/if
   ;; named group
   ifi-present? (s/constrained
                 {(s/required-key "objectType") (s/eq "Group") ;; Group
                  (s/optional-key "name") s/Str
                  (s/optional-key "mbox") MailToIRI
                  (s/optional-key "mbox_sha1sum") Sha1Sum
                  (s/optional-key "openid") OpenID
                  (s/optional-key "account") Account
                  (s/optional-key "member") [Agent]}
                 no-multi-ifi?
                 :predicates/no-multi-ifi)
   ;; anon group
   {(s/required-key "objectType") (s/eq "Group") ;; Group
    (s/optional-key "name") s/Str
    (s/required-key "member") [(s/one Agent :predicates/at-least-one-agent) Agent]}))


(s/defschema
  AbstractActor
  {(s/optional-key "name") s/Str
   (s/optional-key "mbox") MailToIRI
   (s/optional-key "mbox_sha1sum") Sha1Sum
   (s/optional-key "openid") OpenID
   (s/optional-key "account") Account
   (s/optional-key "member") [Agent]})

(s/defschema
  Actor
  (s/conditional
   group?  Group
   agent-actor? Agent
   :else (merge
          AbstractActor
          {(s/optional-key "objectType")
           (s/enum "Agent" "Group") ;; will fail
           })))


(s/defschema
  Verb
  {(s/required-key "id") IRI
   (s/optional-key "display") LanguageMap})


(s/defschema
  Score
  (-> {(s/optional-key "scaled") s/Num ; Decimal number between -1 and 1, inclusive
       (s/optional-key "raw") s/Num ; Decimal number between min and max (if present, otherwise unrestricted), inclusive
       (s/optional-key "min") s/Num ; Decimal number less than max (if present)
       (s/optional-key "max") s/Num}  ; Decimal number greater than min (if present)
      ;; TODO: fix these pred names?
      (s/constrained score-raw-lte-max :predicates/score-lt-max)
      (s/constrained score-raw-gte-min :predicates/score-gt-min)
      (s/constrained score-min-lt-max  :predicates/score-lt-max)))

(s/defschema
  Result
  {(s/optional-key "score") Score
   (s/optional-key "success") s/Bool
   (s/optional-key "completion") s/Bool
   (s/optional-key "response") s/Str
   (s/optional-key "duration") Duration
   (s/optional-key "extensions") Extensions})

(s/defschema
  AbstractStatementRef
  {(s/optional-key "id") s/Str})

(s/defschema
  StatementRef
  {(s/required-key "id") UuidId
   (s/required-key "objectType") (s/eq "StatementRef")})


(s/defschema
  ContextActivitiesArray
  [(s/one Activity :predicates/at-least-one-activity) Activity])

(s/defschema
  ContextActivities
  (s/if map?
    Activity
    ContextActivitiesArray))

(s/defschema
  ContextActivitiesMap
  {(s/optional-key "parent") ContextActivities
   (s/optional-key "grouping") ContextActivities
   (s/optional-key "category") ContextActivities
   (s/optional-key "other") ContextActivities})

(s/defschema
  Context
  {(s/optional-key "registration") UuidId
   (s/optional-key "instructor") Actor
   (s/optional-key "team") Group
   (s/optional-key "contextActivities") ContextActivitiesMap
   (s/optional-key "revision") s/Str
   (s/optional-key "platform") s/Str
   (s/optional-key "language") LanguageTag
   (s/optional-key "statement") StatementRef
   (s/optional-key "extensions") Extensions})

(s/defschema
  Attachment
  {(s/required-key "usageType") IRI
   (s/required-key "display") LanguageMap
   (s/optional-key "description") LanguageMap
   (s/required-key "contentType") s/Str ; Internet Media Type
   (s/required-key "length") s/Int ; The length of the attachment data in octets.
   (s/required-key "sha2") Sha2 ; The SHA-2 (SHA-256, SHA-384, SHA-512) hash of the attachment data.
   (s/optional-key "fileUrl") IRL})

(s/defschema
  UrlAttachment
  {(s/required-key "usageType") IRI
   (s/required-key "display") LanguageMap
   (s/optional-key "description") LanguageMap
   (s/required-key "contentType") s/Str ; Internet Media Type
   (s/required-key "length") s/Int ; The length of the attachment data in octets.
   (s/required-key "sha2") Sha2 ; The SHA-2 (SHA-256, SHA-384, SHA-512) hash of the attachment data.
   (s/required-key "fileUrl") IRL})

(s/defschema
  Attachments
  [(s/one Attachment :predicates/at-least-one-attachement) Attachment])

(s/defschema
  AbstractSubStatement
  {(s/optional-key "actor") Actor
   (s/optional-key "verb") Verb
   (s/optional-key "object")
   (s/conditional
    agent? Agent
    group? Group
    statement-ref? StatementRef
    activity-object? Activity
    :else (merge
           AbstractActor
           AbstractStatementRef
           AbstractActivity
           {(s/required-key "objectType")
            (s/enum "Agent" "Group" "StatementRef" "Activity")}))
   (s/optional-key "result") Result
   (s/optional-key "context") Context
   (s/optional-key "attachments") Attachments
   (s/optional-key "timestamp") Timestamp})

(s/defschema
  SubStatement
  {(s/required-key "actor") Actor
   (s/required-key "verb") Verb
   (s/required-key "object")
   (s/conditional
    agent? Agent
    group? Group
    statement-ref? StatementRef
    activity-object? Activity
    :else (merge
           AbstractActor
           AbstractStatementRef
           AbstractActivity
           {(s/required-key "objectType")
            (s/enum "Agent" "Group" "StatementRef" "Activity")}))
   (s/optional-key "result") Result
   (s/optional-key "context") Context
   (s/optional-key "attachments") Attachments
   (s/optional-key "timestamp") Timestamp
   (s/required-key "objectType") (s/eq "SubStatement")})


(s/defschema
  OAuthConsumer
  {(s/optional-key "objectType") (s/eq "Agent") ;; Agent
   (s/optional-key "name") s/Str
   (s/required-key "account") Account})

(s/defschema
  ThreeLeggedOAuthGroup
  {(s/optional-key "objectType") (s/eq "Group") ;; Group
   (s/optional-key "name") s/Str
   (s/optional-key "mbox") MailToIRI
   (s/optional-key "mbox_sha1sum") Sha1Sum
   (s/optional-key "openid") OpenID
   (s/optional-key "account") Account
   (s/required-key "member") (s/constrained
                              [(s/one OAuthConsumer
                                      :predicates/one-oauth-consumer) Agent]
                              two-members?
                              :predicates/exactly-2-members)})

(s/defschema
  Authority
  (s/if
   group? ThreeLeggedOAuthGroup
   Agent))

(s/defschema
  StatementObject
  (s/conditional
   agent? Agent
   group? Group
   sub-statement? SubStatement
   statement-ref? StatementRef
   activity-object? Activity
   :else (merge
          AbstractActor
          AbstractStatementRef
          AbstractActivity
          AbstractSubStatement
          {(s/required-key "objectType")
           (s/enum "Agent" "Group" "StatementRef" "SubStatement" "Activity")})))


(s/defschema
  Statement
  (-> {(s/optional-key "id") UuidId
       (s/required-key "actor") Actor
       (s/required-key "verb") Verb
       (s/required-key "object") StatementObject
       (s/optional-key "result") Result
       (s/optional-key "context") Context
       (s/optional-key "timestamp") Timestamp
       (s/optional-key "stored") Timestamp
       (s/optional-key "authority") Authority
       (s/optional-key "version") Version
       (s/optional-key "attachments") Attachments
       (s/optional-key "objectType") s/Str ;; necessary for validating substatements as statements!
       }
      (s/constrained valid-void? :predicates/void-statement-ref)
      (s/constrained valid-revision? :predicates/revision-not-allowed)
      (s/constrained valid-platform? :predicates/platform-not-allowed)))

(s/defschema
  Statements
  [(s/one Statement :predicates/at-least-one-statement)
   Statement])
