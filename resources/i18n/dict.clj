{:en {;; top level
      :not "not"
      :threw "threw"

      ;; keys
      :missing-required-key "Missing required key"
      :disallowed-key "Key not allowed"
      :invalid-key "Key invalid"

      ;; explanation predicates
      :sequential? "sequential?"
      :map? "map?"
      :integer? "integer?"
      :string? "string?"
      :number? "number?"
      :boolean? "boolean?"
      :in "in"
      :present? "present?"
      := "equal to"
      :re-find "found in RegEx"
      :reason "reason"

      :predicates
      {:revision-not-allowed "valid if Statement object is an Activity"
       :platform-not-allowed "valid if Statement object is an Activity"
       :no-multi-ifi "there is at most one IFI"
       :no-ifi "present: IFI"
       :no-anon-group-member "present: member key on anonymous group"
       :distinct-ic-ids "distinct: Interaction Component IDs"
       :valid-component-keys  "valid Interaction Component List key(s)"
       :score-lt-max "less than max"
       :score-gt-min "greater than min"
       :exactly-2-members "Exactly 2 Members"
       :at-least-one-agent "at least one Agent"
       :at-least-one-activity "at least one Activity"
       :at-least-one-attachment "at least one Attachment"
       :one-oauth-consumer "one OAuth Consumer"
       :at-least-one-statement "at least one statement"
       :void-statement-ref "a valid voiding statement with objectType StatementRef"}

      :xapi
      {:language-map "Language Map"
       :extensions "Extensions Map"
       :interaction-component "Interaction Component"
       :interaction-components "Interaction Components Array"
       :definition "Activity Definition"
       :activity "Activity"
       :account "Account"
       :agent "Agent"
       :group "Group"
       :actor "Actor"
       :verb "Verb"
       :score "Score"
       :result "Result"
       :statement-ref "Statement Reference"
       :context-activities-array "Context Activities Array"
       :context-activities-single-or-array "Context Activities Array or Single Activity"
       :context-activities-map "Context Activities Map"
       :context "Context"
       :file-attachment "File Attachment"
       :url-attachment "URL Attachment"
       :attachments "Attachments Array"
       :sub-statement "SubStatement"
       :oauth-consumer "OAuth Consumer Agent"
       :three-legged-oauth-group "Three Legged OAuth Group"
       :authority "Authority"
       :statement-object "Statement Object"
       :statement "Statement"
       :statements "Statements"}

      :valid
      {:ltag "a valid RFC 5646 Language Tag"
       :iri "a valid IRI address"
       :mailto-iri "a valid MailTo IRI address"
       :irl "a valid IRL"
       :openid "a valid OpenID URL"
       :uuid "a valid Uuid"
       :timestamp "a valid ISO 8601 timestamp"
       :duration "a valid ISO 8601 duration"
       :xapi-version "a valid xAPI 1.x.x version"
       :sha-2-sum "a valid SHA-2 sum"
       :sha-1-sum "a valid SHA-1 sum"}

      :valid-key
      {:ltag "a valid RFC 5646 Language Tag key"
       :iri "a valid IRI address key"}}}
