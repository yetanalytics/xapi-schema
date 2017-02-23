(ns xapi-schema.schemata.json
  (:require
   [xapi-schema.schemata.predicates :refer [re-pred
                                            unique-ids?
                                            valid-component-keys?
                                            no-multi-ifi?
                                            ifi-present?
                                            has-members?
                                            valid-score?
                                            map-not-empty?
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
   [xapi-schema.schemata.util :refer [check-type object-type-dispatch]]
   #?(:clj [schema.core :as s]
      :cljs [schema.core :as s
             :include-macros true])))


;; Component schema

(s/defschema
  LanguageTag
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#52-language-map"
  (s/named
   (s/constrained
    s/Str
    (re-pred LanguageTagRegEx)
    :predicates/valid-ltag)
   "Language Tag"))

(s/defschema
  LanguageMap
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#52-language-map"
  (s/named
   {LanguageTag s/Str}
   "Language Map"))

(s/defschema
  IRI
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#def-iri"
  (s/named
   (s/constrained
    s/Str
    (re-pred AbsoluteIRIRegEx)
    :predicates/valid-iri)
   "Internationalized Resource Locator"))

(s/defschema
  MailToIRI
  "http://xmlns.com/foaf/spec/#term_mbox"
  (s/named
   (s/constrained
    s/Str
    (re-pred MailToIRIRegEx)
    :predicates/valid-mailto-iri)
   "Mailto IRI"))

(s/defschema
  IRL
  (s/named
   (s/constrained
    s/Str
    (re-pred AbsoluteIRIRegEx)
    :predicates/valid-irl)
   "IRL"))

(s/defschema
  Extensions
  (s/named
   {IRI s/Any}
   "Extensions Map"))

(s/defschema
  OpenID
  (s/named
   (s/constrained s/Str
                  (re-pred OpenIdRegEx)
                  :predicates/valid-openid)
   "OpenId URL"))

(s/defschema
  UuidId
  (s/named
   (s/constrained s/Str
                  (re-pred UuidRegEx)
                  :predicates/valid-uuid)
   "Uuid"))

(s/defschema
  Timestamp
  (s/named
   (s/constrained s/Str
                  (re-pred TimestampRegEx)
                  :predicates/valid-timestamp)
   "Timestamp"))

(s/defschema
  Duration
  (s/named
   (s/constrained s/Str
                  (re-pred DurationRegEx)
                  :predicates/valid-duration)
   "Duration"))

(s/defschema
  Version
  (s/named
   (s/constrained s/Str
                  (re-pred xAPIVersionRegEx)
                  :predicates/valid-xapi-version)
   "Version"))

(s/defschema
  Sha2
  (s/named
   (s/constrained s/Str
                  (re-pred Base64RegEx)
                  :predicates/valid-sha-2-sum)
   "Sha2"))

(def
  Sha1Sum
  (s/named
   (s/constrained s/Str
                  (re-pred Sha1RegEx)
                  :predicates/valid-sha-1-sum)
   "SHA-1 Sum"))

;; Composite schemas

(s/defschema
  InteractionComponent
  (s/named
   {(s/required-key "id") s/Str
    (s/optional-key "description") LanguageMap}
   "Interaction Component"))

(s/defschema
  InteractionComponents
  (s/named
   (s/constrained [InteractionComponent]
                  unique-ids?
                  :predicates/distinct-ic-ids)
   "Interaction Components Array"))

(s/defschema
  Definition
  (s/named
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
    :predicates/valid-component-keys)
   "Activity Definition"))

(s/defschema
  Activity
  (s/named
   {(s/optional-key "objectType") (s/eq "Activity")
    (s/required-key "id") IRI
    (s/optional-key "definition") Definition}
   "Activity Definition"))

(s/defschema
  Account
  (s/named
   {(s/required-key "homePage") IRL
    (s/required-key "name") s/Str}
   "Account"))


(s/defschema
  Agent
  (s/named
   (-> {(s/optional-key "objectType") (s/eq "Agent")
        (s/optional-key "name") s/Str
        (s/optional-key "mbox") MailToIRI
        (s/optional-key "mbox_sha1sum") Sha1Sum
        (s/optional-key "openid") OpenID
        (s/optional-key "account") Account}

       (s/constrained ifi-present? :predicates/no-ifi)
       (s/constrained no-multi-ifi? :predicates/no-multi-ifi))
   "Agent"))

(s/defschema
  Group
  (s/named
   (s/conditional
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
    :else (s/constrained
           {(s/required-key "objectType") (s/eq "Group") ;; Group
            (s/optional-key "name") s/Str
            (s/optional-key "member") [(s/one Agent :predicates/at-least-one-agent) Agent]}
           has-members?
           :predicates/no-anon-group-member))
   "Group"))

(s/defschema
  Actor
  (s/named
   (object-type-dispatch "Group" Group
                         :else Agent)
   "Actor"))


(s/defschema
  Verb
  (s/named
   {(s/required-key "id") IRI
    (s/optional-key "display") LanguageMap}
   "Verb"))


(s/defschema
  Score
  (s/named
   (-> {(s/optional-key "scaled") s/Num ; Decimal number between -1 and 1, inclusive
        (s/optional-key "raw") s/Num ; Decimal number between min and max (if present, otherwise unrestricted), inclusive
        (s/optional-key "min") s/Num ; Decimal number less than max (if present)
        (s/optional-key "max") s/Num}  ; Decimal number greater than min (if present)
       ;; TODO: fix these pred names?
       (s/constrained valid-score? :predicates/valid-score))
   "Score"))

(s/defschema
  Result
  (s/named
   {(s/optional-key "score") Score
    (s/optional-key "success") s/Bool
    (s/optional-key "completion") s/Bool
    (s/optional-key "response") s/Str
    (s/optional-key "duration") Duration
    (s/optional-key "extensions") Extensions}
   "Result"))

(s/defschema
  StatementRef
  (s/named
   {(s/required-key "id") UuidId
    (s/required-key "objectType") (s/eq "StatementRef")}
   "Statement Reference"))


(s/defschema
  ContextActivitiesArray
  (s/named
   [(s/one Activity :predicates/at-least-one-activity) Activity]
   "Context Activities Array"))

(s/defschema
  ContextActivities
  (s/named
   (s/if map?
     Activity
     ContextActivitiesArray)
   "Context Activities"))

(s/defschema
  ContextActivitiesMap
  (s/named
   (-> {(s/optional-key "parent") ContextActivities
        (s/optional-key "grouping") ContextActivities
        (s/optional-key "category") ContextActivities
        (s/optional-key "other") ContextActivities}
       (s/constrained map-not-empty? :predicates/empty-map-not-allowed))
   "Context Activities Map"))

(s/defschema
  Context
  (s/named
   {(s/optional-key "registration") UuidId
    (s/optional-key "instructor") Actor
    (s/optional-key "team") Group
    (s/optional-key "contextActivities") ContextActivitiesMap
    (s/optional-key "revision") s/Str
    (s/optional-key "platform") s/Str
    (s/optional-key "language") LanguageTag
    (s/optional-key "statement") StatementRef
    (s/optional-key "extensions") Extensions}
   "Context"))

(s/defschema
  Attachment
  (s/named
   {(s/required-key "usageType") IRI
    (s/required-key "display") LanguageMap
    (s/optional-key "description") LanguageMap
    (s/required-key "contentType") s/Str ; Internet Media Type
    (s/required-key "length") s/Int ; The length of the attachment data in octets.
    (s/required-key "sha2") Sha2 ; The SHA-2 (SHA-256, SHA-384, SHA-512) hash of the attachment data.
    (s/optional-key "fileUrl") IRL}
   "File Attachment"))

(s/defschema
  UrlAttachment
  (s/named
   {(s/required-key "usageType") IRI
    (s/required-key "display") LanguageMap
    (s/optional-key "description") LanguageMap
    (s/required-key "contentType") s/Str ; Internet Media Type
    (s/required-key "length") s/Int ; The length of the attachment data in octets.
    (s/required-key "sha2") Sha2 ; The SHA-2 (SHA-256, SHA-384, SHA-512) hash of the attachment data.
    (s/required-key "fileUrl") IRL}
   "URL Attachment"))

(s/defschema
  Attachments
  (s/named
   [(s/one Attachment :predicates/at-least-one-attachement) Attachment]
   "Attachments Array"))

(s/defschema
  SubStatement
  (s/named
   (-> {(s/required-key "actor") Actor
        (s/required-key "verb") Verb
        (s/required-key "object")
        (object-type-dispatch "Agent" Agent
                              "Group" Group
                              "StatementRef" StatementRef
                              :else Activity)
        (s/optional-key "result") Result
        (s/optional-key "context") Context
        (s/optional-key "attachments") Attachments
        (s/optional-key "timestamp") Timestamp
        (s/required-key "objectType") (s/eq "SubStatement")}
       (s/constrained valid-revision? :predicates/revision-not-allowed)
       (s/constrained valid-platform? :predicates/platform-not-allowed))
   "SubStatement"))

(s/defschema
  OAuthConsumer
  (s/named
   {(s/optional-key "objectType") (s/eq "Agent") ;; Agent
    (s/optional-key "name") s/Str
    (s/required-key "account") Account}
   "OAuth Consumer Agent"))

(s/defschema
  ThreeLeggedOAuthGroup
  (s/named
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
                               :predicates/exactly-2-members)}
   "Three-Legged OAuth Group"))

(s/defschema
  Authority
  (s/named
   (object-type-dispatch
    "Agent" Agent
    "Group" ThreeLeggedOAuthGroup
    :else Agent)
   "Authority"))

(s/defschema
  StatementObject
  (s/named
   (object-type-dispatch
    "Agent" Agent
    "Group" Group
    "SubStatement" SubStatement
    "StatementRef" StatementRef
    "Activity" Activity
    :else Activity)
   "Statement Object"))


(s/defschema
  Statement
  (s/named
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
       (s/constrained valid-platform? :predicates/platform-not-allowed))
   "Statement"))

(s/defschema
  Statements
  (s/named
   [(s/one Statement :predicates/at-least-one-statement)
    Statement]
   "Statements"))
