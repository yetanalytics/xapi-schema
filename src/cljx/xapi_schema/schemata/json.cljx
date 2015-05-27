(ns xapi-schema.schemata.json
  (:require
   [xapi-schema.schemata.predicates :refer [regex-pred
                                            InteractionComponentsValidations
                                            DefinitionValidations
                                            AgentValidations
                                            GroupValidations
                                            ScoreValidations
                                            StatementValidations]]
   [xapi-schema.schemata.regex :refer [LanguageTagRegEx
                                       URIRegEx
                                       AbsoluteIRIRegEx
                                       MailToIRIRegEx
                                       UuidRegEx
                                       TimestampRegEx
                                       xAPIVersionRegEx
                                       DurationRegEx
                                       Base64RegEx
                                       Sha1RegEx]]
   [xapi-schema.schemata.util :refer [check-type object-type-dispatch]]
   #+clj [schema.core :as s]
   #+cljs [schema.core :as s
           :include-macros true]))


;; Component schema

(s/defschema
  LanguageTag
  "https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI.md#52-language-map"
  (s/named
   (s/both
    s/Str
    (regex-pred LanguageTagRegEx
                :predicates/valid-ltag))
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
   (s/both
    s/Str
    (regex-pred AbsoluteIRIRegEx
                :predicates/valid-iri))
   "Internationalized Resource Locator"))

(s/defschema
  MailToIRI
  "http://xmlns.com/foaf/spec/#term_mbox"
  (s/named
   (s/both
    s/Str
    (regex-pred MailToIRIRegEx
                :predicates/valid-mailto-iri))
   "Mailto IRI"))

(s/defschema
  IRL
  (s/named
   (s/both
    (regex-pred AbsoluteIRIRegEx
                :predicates/valid-irl)
    s/Str)
   "IRL"))

(s/defschema
  Extensions
  (s/named
   {IRI s/Any}
   "Extensions Map"))

(s/defschema
  OpenID
  (s/named
   (s/both s/Str
           (regex-pred URIRegEx
                       :predicates/valid-openid))
   "OpenId URL"))

(s/defschema
  UuidId
  (s/named
   (s/both s/Str
           (regex-pred UuidRegEx
                       :predicates/valid-uuid))
   "Uuid"))

(s/defschema
  Timestamp
  (s/named
   (s/both s/Str
           (regex-pred TimestampRegEx
                       :predicates/valid-timestamp))
   "Timestamp"))

(s/defschema
  Duration
  (s/named
   (s/both s/Str
           (regex-pred DurationRegEx
                       :predicates/valid-duration))
   "Duration"))

(s/defschema
  Version
  (s/named
   (s/both s/Str
           (regex-pred xAPIVersionRegEx
                       :predicates/valid-xapi-version))
   "Version"))

(s/defschema
  Sha2
  (s/both (regex-pred Base64RegEx
                      :predicates/valid-sha-2-sum)
          s/Str))

(def
  Sha1Sum
  (s/named
   (s/both s/Str
           (regex-pred Sha1RegEx
                       :predicates/valid-sha-1-sum))
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
   (s/both [InteractionComponent]
           InteractionComponentsValidations)
   "Interaction Components Array"))

(s/defschema
  Definition
  (s/named
   (s/both
    {(s/optional-key "name") LanguageMap
     (s/optional-key "description") LanguageMap
     (s/optional-key "correctResponsesPattern") [s/Str]
     (s/optional-key "interactionType")
     (s/both s/Str
             (s/enum "true-false"
                     "choice"
                     "fill-in"
                     "long-fill-in"
                     "matching"
                     "performance"
                     "sequencing"
                     "likert"
                     "numeric"
                     "other"))
     (s/optional-key "type") IRI
     (s/optional-key "moreInfo") IRL
     (s/optional-key "choices") InteractionComponents
     (s/optional-key "scale") InteractionComponents
     (s/optional-key "source") InteractionComponents
     (s/optional-key "target") InteractionComponents
     (s/optional-key "steps") InteractionComponents
     (s/optional-key "extensions") Extensions}
    DefinitionValidations)
   "Activity Definition"))

(s/defschema
  Activity
  (s/named
   {(s/optional-key "objectType") (s/both s/Str (s/eq "Activity"))
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
   (s/both
    {(s/optional-key "objectType")
     (s/both s/Str (s/both
                    s/Str
                    (s/eq "Agent"))) ;; Agent
     (s/optional-key "name") s/Str
     (s/optional-key "mbox") MailToIRI
     (s/optional-key "mbox_sha1sum") Sha1Sum
     (s/optional-key "openid") OpenID
     (s/optional-key "account") Account}
    AgentValidations)
   "Agent"))

(s/defschema
  Group
  (s/named
   (s/both
    {(s/optional-key "objectType") (s/both s/Str (s/eq "Group")) ;; Group
     (s/optional-key "name") s/Str
     (s/optional-key "mbox") MailToIRI
     (s/optional-key "mbox_sha1sum") Sha1Sum
     (s/optional-key "openid") OpenID
     (s/optional-key "account") Account
     (s/optional-key "member") [(s/one Agent :predicates/at-least-one-agent) Agent]}
    GroupValidations)
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
   (s/both {(s/optional-key "scaled") s/Num ; Decimal number between -1 and 1, inclusive
            (s/optional-key "raw") s/Num ; Decimal number between min and max (if present, otherwise unrestricted), inclusive
            (s/optional-key "min") s/Num ; Decimal number less than max (if present)
            (s/optional-key "max") s/Num}  ; Decimal number greater than min (if present)
           ScoreValidations)
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
    (s/required-key "objectType") (s/both
                                   s/Str
                                   (s/eq "StatementRef"))}
   "Statement Reference"))

(s/defschema
  ContextStatementRef
  (s/named
   {(s/required-key "id") UuidId
    (s/optional-key "objectType") (s/both
                                   s/Str
                                   (s/eq "StatementRef"))}
   "Context Statement Reference"))


(s/defschema
  ContextActivitiesArray
  (s/named
   [(s/one Activity :predicates/at-least-one-activity) Activity]
   "Context Activities Array"))

(s/defschema
  ContextActivities
  (s/named
   {(s/optional-key "parent") ContextActivitiesArray
    (s/optional-key "grouping") ContextActivitiesArray
    (s/optional-key "category") ContextActivitiesArray
    (s/optional-key "other") ContextActivitiesArray}
   "Context Activities Object"))

(s/defschema
  Context
  (s/named
   {(s/optional-key "registration") UuidId
    (s/optional-key "instructor") Actor
    (s/optional-key "team") Group
    (s/optional-key "contextActivities") ContextActivities
    (s/optional-key "revision") s/Str
    (s/optional-key "platform") s/Str
    (s/optional-key "language") LanguageTag
    (s/optional-key "statement") ContextStatementRef
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
   {(s/required-key "actor") Actor
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
    (s/required-key "objectType") (s/both s/Str (s/eq "SubStatement"))}
   "SubStatement"))

(s/defschema
  OAuthConsumer
  (s/named
   {(s/optional-key "objectType") (s/both s/Str (s/eq "Agent")) ;; Agent
    (s/optional-key "name") s/Str
    (s/required-key "account") Account}
   "OAuth Consumer Agent"))

(s/defschema
  ThreeLeggedOAuthGroup
  (s/named
   (s/both
    GroupValidations
    {(s/optional-key "objectType") (s/both s/Str (s/eq "Group")) ;; Group
     (s/optional-key "name") s/Str
     (s/optional-key "mbox") MailToIRI
     (s/optional-key "mbox_sha1sum") Sha1Sum
     (s/optional-key "openid") OpenID
     (s/optional-key "account") Account
     (s/required-key "member") (s/both
                                (s/pred (fn [m]
                                          (= 2 (count m))) :predicates/exactly-2-members)
                                [(s/one OAuthConsumer
                                        :predicates/one-oauth-consument) Agent])})
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
   (s/both
    {(s/optional-key "id") UuidId
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
    StatementValidations)
   "Statement"))

(s/defschema
  Statements
  (s/named
   [(s/one Statement :predicates/at-least-one-statement)
    Statement]
   "Statements"))
