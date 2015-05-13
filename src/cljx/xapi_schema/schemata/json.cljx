(ns xapi-schema.schemata.json
  (:require
   [xapi-schema.schemata.predicates :refer [regex-pred
                                            InteractionComponentsValidations
                                            DefinitionValidations
                                            ActivityValidations
                                            AgentValidations
                                            GroupValidations
                                            ScoreValidations
                                            StatementValidations
                                            SubStatementValidations
                                            AuthorityGroupValidations]]
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

(s/defschema LanguageTag
  (s/both (regex-pred LanguageTagRegEx
                      "a valid language tag")
          s/Str))

(s/defschema LanguageMap
  {LanguageTag s/Str})

(s/defschema IRI
  (s/both (regex-pred AbsoluteIRIRegEx
                      "a valid IRI")
          s/Str))

(s/defschema MailToIRI
  (s/both (regex-pred MailToIRIRegEx
                      "a valid MailTo IRI")
          s/Str))

(s/defschema IRL
  (s/both (regex-pred AbsoluteIRIRegEx
                      "a valid IRL")
          s/Str))

(s/defschema Extensions
  {IRI s/Any})

(s/defschema OpenID
  (s/both (regex-pred URIRegEx
                      "a valid OpenID URL")
          s/Str))

(s/defschema Uuid
  (s/both (regex-pred UuidRegEx
                      "a valid UUID")
          s/Str))

(s/defschema Timestamp
  (s/both (regex-pred TimestampRegEx
                      "a valid ISO 8601 timestamp")
          s/Str))

(s/defschema Duration
  (s/both (regex-pred DurationRegEx
                      "a valid ISO 8601 duration")
          s/Str))

(s/defschema Version
  (s/both (regex-pred xAPIVersionRegEx
                      "a valid xAPI 1.x.x version")
          s/Str))

(s/defschema Sha2
  (s/both (regex-pred Base64RegEx
                      "a valid SHA-2 sum")
          s/Str))

;; (s/defschema Sha1Sum ;; TODO: fix this regex
;;   (s/both (v/regex-pred v/Sha1RegEx
;;                         "a valid mbox_sha1sum")
;;           s/Str))

(def Sha1Sum
  s/Str)

;; Composite schemas


(s/defschema InteractionComponents
  (s/both InteractionComponentsValidations
          #{{(s/required-key "id") s/Str
            (s/optional-key "description") LanguageMap}}))

(s/defschema Definition
  (s/both DefinitionValidations
          {(s/optional-key "name") LanguageMap
           (s/optional-key "description") LanguageMap
           (s/optional-key "correctResponsesPattern") #{s/Str}
           (s/optional-key "interactionType") (s/both s/Str (s/enum "true-false" "choice" "fill-in" "long-fill-in" "matching" "performance" "sequencing" "likert" "numeric" "other"))
           (s/optional-key "type") IRI
           (s/optional-key "moreInfo") IRL
           (s/optional-key "choices") InteractionComponents
           (s/optional-key "scale") InteractionComponents
           (s/optional-key "source") InteractionComponents
           (s/optional-key "target") InteractionComponents
           (s/optional-key "steps") InteractionComponents
           (s/optional-key "extensions") Extensions}))

(s/defschema Activity
  (s/both ActivityValidations
          {(s/optional-key "objectType") (s/both s/Str (s/eq "Activity"))
           (s/required-key "id") IRI
           (s/optional-key "definition") Definition}))

(s/defschema Account
  {(s/required-key "homePage") IRL
   (s/required-key "name") s/Str})


(s/defschema Agent
  (s/both AgentValidations
          {(s/optional-key "objectType") (s/both s/Str (s/eq "Agent")) ;; Agent
           (s/optional-key "name") s/Str
           (s/optional-key "mbox") MailToIRI
           (s/optional-key "mbox_sha1sum") Sha1Sum
           (s/optional-key "openid") OpenID
           (s/optional-key "account") Account}))

(s/defschema Group
  (s/both {(s/optional-key "objectType") (s/both s/Str (s/eq "Group")) ;; Group
           (s/optional-key "name") s/Str
           (s/optional-key "mbox") MailToIRI
           (s/optional-key "mbox_sha1sum") Sha1Sum
           (s/optional-key "openid") OpenID
           (s/optional-key "account") {(s/required-key "homePage") IRL
                                       (s/required-key "name") s/Str}
           (s/optional-key "member") #{Agent}}
          GroupValidations))

(s/defschema Actor
  (object-type-dispatch "Group" Group
                        :else Agent))


(s/defschema Verb
  {(s/required-key "id") IRI
   (s/optional-key "display") LanguageMap})


(s/defschema Score (s/both ScoreValidations
                   {(s/optional-key "scaled") s/Num ; Decimal number between -1 and 1, inclusive
                    (s/optional-key "raw") s/Num ; Decimal number between min and max (if present, otherwise unrestricted), inclusive
                    (s/optional-key "min") s/Num ; Decimal number less than max (if present)
                    (s/optional-key "max") s/Num}) ; Decimal number greater than min (if present)
  )

(s/defschema Result
  {(s/optional-key "score") Score
   (s/optional-key "success") s/Bool
   (s/optional-key "completion") s/Bool
   (s/optional-key "response") s/Str
   (s/optional-key "duration") Duration
   (s/optional-key "extensions") Extensions})

(s/defschema StatementRef
  {(s/required-key "id") Uuid
   (s/required-key "objectType") (s/eq "StatementRef")})

(s/defschema ContextStatementRef
  {(s/required-key "id") Uuid
   (s/optional-key "objectType") (s/eq "StatementRef")})


(s/defschema ContextActivitiesArray #{Activity})

(s/defschema ContextActivities
  {(s/optional-key "parent") ContextActivitiesArray
   (s/optional-key "grouping") ContextActivitiesArray
   (s/optional-key "category") ContextActivitiesArray
   (s/optional-key "other") ContextActivitiesArray})

(s/defschema Context
  {(s/optional-key "registration") Uuid
   (s/optional-key "instructor") Actor
   (s/optional-key "team") Group
   (s/optional-key "contextActivities") ContextActivities
   (s/optional-key "revision") s/Str
   (s/optional-key "platform") s/Str
   (s/optional-key "language") LanguageTag
   (s/optional-key "statement") ContextStatementRef
   (s/optional-key "extensions") Extensions})

(s/defschema Attachment
  {(s/required-key "usageType") IRI
   (s/required-key "display") LanguageMap
   (s/optional-key "description") LanguageMap
   (s/required-key "contentType") s/Str ; Internet Media Type
   (s/required-key "length") s/Int ; The length of the attachment data in octets.
   (s/required-key "sha2") Sha2 ; The SHA-2 (SHA-256, SHA-384, SHA-512) hash of the attachment data.
   (s/optional-key "fileUrl") IRL})

(s/defschema UrlAttachment
  {(s/required-key "usageType") IRI
   (s/required-key "display") LanguageMap
   (s/optional-key "description") LanguageMap
   (s/required-key "contentType") s/Str ; Internet Media Type
   (s/required-key "length") s/Int ; The length of the attachment data in octets.
   (s/required-key "sha2") Sha2 ; The SHA-2 (SHA-256, SHA-384, SHA-512) hash of the attachment data.
   (s/required-key "fileUrl") IRL})

(s/defschema Attachments
  #{Attachment})

(s/defschema SubStatement
  (s/both {(s/required-key "actor") Actor
           (s/required-key "verb") Verb
           (s/required-key "object") (object-type-dispatch "Agent" Agent
                                                           "Group" Group
                                                           "StatementRef" StatementRef
                                                           :else Activity)
           (s/optional-key "result") Result
           (s/optional-key "context") Context
           (s/optional-key "attachments") Attachments
           (s/optional-key "timestamp") Timestamp
           (s/required-key "objectType") (s/both s/Str (s/eq "SubStatement"))}
          SubStatementValidations))

;; (s/defschema OAuthConsumer
;;   (s/both
;;    Agent
;;    v/OAuthConsumerValidations))

;; (s/defschema Authority
;;   (object-type-dispatch "Group" (s/both v/AuthorityGroupValidations
;;                                         {(s/optional-key "objectType") (s/both s/Str (s/enum "Agent" "Group")) ;; Agent or Group
;;                                          (s/optional-key "name") s/Str
;;                                          (s/optional-key "mbox") MailToIRI
;;                                          (s/optional-key "mbox_sha1sum") Sha1Sum
;;                                          (s/optional-key "openid") OpenID
;;                                          (s/optional-key "account") Account
;;                                          (s/optional-key "member") [(s/one OAuthConsumer :oauth-consumer) Agent]})
;;                         :else OAuthConsumer))

(s/defschema Authority
  {s/Any s/Any})

(s/defschema StatementObject
  (object-type-dispatch "Agent" Agent
                        "Group" Group
                        "SubStatement" SubStatement
                        "StatementRef" StatementRef
                        "Activity" Activity
                        :else Activity))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statement... the main attraction!

(s/defschema Statement
  "Type map ONLY schema for an xAPI Statement object, as implied in the xAPI Spec section 4.0 Statements [https://github.com/adlnet/xAPI-Spec/blob/master/xAPI.md#40-statement]"
  (s/both {(s/optional-key "id") Uuid
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
          StatementValidations))

(s/defschema Statements
  #{Statement})
