(ns xapi-schema.schemata.spec
  (:require
   [xapi-schema.schemata.predicates :refer [re-pred]]
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
   [clojure.set :refer [intersection
                        difference]]
   #?(:clj [clojure.spec :as s]
      :cljs
      [cljs.spec :as s :include-macros true])))


(defn namespace-conformer
  "Returns a conformer that coerces map keys to the given ns."
  [nss]
  (s/conformer
   (fn [m]
     (try
       (into {}
             (for [[k v] m]
               [(keyword nss k) v]))
       (catch Exception _
         :clojure.spec/invalid)))
   (fn unf [m]
     (if (map? m)
       (into {}
             (for [[k v] m]
               [(name k) v]))
       (recur (second m))))))

(def strip-tags-conformer
  (s/conformer
   identity
   val
   #_(fn [x]
     (if (and (some-> x first keyword?))
       (recur (second x))
       x))))

;; Leaves

(s/def ::language-tag
  (s/and string?
         (re-pred LanguageTagRegEx)))

(s/def ::language-map
  (s/map-of ::language-tag
            string?
            :gen-max 3))

(s/def ::iri
  (s/and string?
         (re-pred AbsoluteIRIRegEx)))

(s/def ::mailto-iri
  (s/and string?
         (re-pred MailToIRIRegEx)))

(s/def ::irl
  (s/and string?
         (re-pred AbsoluteIRIRegEx)))

(s/def ::any-json
  (s/and
   (s/or :scalar (s/and
                  (s/or :string
                        string?
                        :number
                        (s/and
                         (s/or :double
                               double?
                               :int
                               int?)
                         strip-tags-conformer))
                  strip-tags-conformer)
         :coll (s/and
                (s/or :map
                    (s/map-of
                     string?
                     ::any-json
                     :gen-max 4)
                    :vector
                    (s/coll-of
                     ::any-json
                     :kind vector?
                     :into []
                     :gen-max 4))
                strip-tags-conformer))
   strip-tags-conformer))

(s/def ::extensions
  (s/map-of ::iri
            ::any-json)) ;; TODO: spec for any valid json

(s/def ::openid
  (s/and string?
         (re-pred OpenIdRegEx)))

(s/def ::uuid
  (s/and string?
         (re-pred UuidRegEx)))

(s/def ::timestamp
  (s/and string?
         (re-pred TimestampRegEx)))

(s/def ::duration
  (s/and string?
         (re-pred DurationRegEx)))

(s/def ::version
  (s/and string?
         (re-pred xAPIVersionRegEx)))

(s/def ::sha2
  (s/and string?
         (re-pred Base64RegEx)))

(s/def ::sha1sum
  (s/and string?
         (re-pred Sha1RegEx)))

;; Activity Definition

(s/def :interaction-component/id
  string?)

(s/def :interaction-component/description
  ::language-map)

(s/def ::interaction-component*
  (s/keys :req [:interaction-component/id]
          :opt [:interaction-component/description]))

(s/def ::interaction-component
  (s/and (namespace-conformer "interaction-component")
         ::interaction-component*))

(s/def ::interaction-components
  (s/and
   (s/coll-of ::interaction-component :kind vector? :into [])
   #(distinct? (map :id %)))
  #_(s/and vector?
         #(distinct? (map :id %))
         (s/cat ::interaction-component (s/* ::interaction-component))
         (s/conformer identity vec)))

(s/def :definition/name
  ::language-map)

(s/def :definition/description
  ::language-map)

(s/def :definition/correctResponsesPattern
  (s/coll-of string? :kind vector?))

(s/def :definition/type
  ::iri)

(s/def :definition/moreInfo
  ::irl)

(s/def :definition/choices
  ::interaction-components)

(s/def :definition/scale
  ::interaction-components)

(s/def :definition/source
  ::interaction-components)

(s/def :definition/target
  ::interaction-components)

(s/def :definition/steps
  ::interaction-components)

(s/def :definition/extensions
  ::extensions)

(s/def :definition/interactionType
  #{"true-false"
    "choice"
    "fill-in"
    "long-fill-in"
    "matching"
    "performance"
    "sequencing"
    "likert"
    "numeric"
    "other"})

(def component-keys
  #{:definition/choices
    :definition/scale
    :definition/target
    :definition/steps
    :definition/source})

(def valid-component-keys
  "Given an interactionType, what component keys are valid?"
  {"choice"      #{:definition/choices}
   "sequencing"  #{:definition/choices}
   "likert"      #{:definition/scale}
   "matching"    #{:definition/source :definition/target}
   "performance" #{:definition/steps}
   "true-false"  #{}
   "fill-in"     #{}
   "numeric"     #{}
   "other"       #{}})

(defn valid-definition-component-keys?
  [data]
  "Predicate to ensure valid component list keys"
  (let [interaction-type (:definition/interactionType data)
        submitted-keys (intersection (set (keys data)) component-keys)
        valid-for-type (valid-component-keys interaction-type)
        invalid (difference submitted-keys valid-for-type)]

    (if (and interaction-type (seq invalid))
      false
      true)))

(s/def :activity/definition*
  (s/and
   (s/keys :opt [:definition/name
                 :definition/description
                 :definition/correctResponsesPattern
                 :definition/interactionType
                 :definition/type
                 :definition/moreInfo
                 :definition/choices
                 :definition/scale
                 :definition/source
                 :definition/target
                 :definition/steps
                 :definition/extensions])
   valid-definition-component-keys?))

(s/def :activity/definition
  (s/and
   (namespace-conformer "definition")
   :activity/definition*))

(s/def :activity/objectType
  #{"Activity"})

(s/def :activity/id
  ::iri)

(s/def ::activity*
  (s/keys :req [:activity/id]
          :opt [:activity/objectType
                :activity/definition]))

(s/def ::activity
  (s/and
   (namespace-conformer "activity")
   ::activity*))

;; Account

(s/def :account/name
  string?)

(s/def :account/homePage
  ::irl)

(s/def ::account*
  (s/keys :req [:account/name
                :account/homePage]))

(s/def ::account
  (s/and (namespace-conformer "account")
         ::account*))

;; Agent

(s/def :agent/objectType
  #{"Agent"})

(s/def :agent/name
  string?)

(s/def :agent/mbox
  ::mailto-iri)

(s/def :agent/mbox_sha1sum
  ::sha1sum)

(s/def :agent/openid
  ::openid)

(s/def :agent/account
  ::account)

(defn one-ifi? [a]
  (= 1 (count (select-keys a [:agent/mbox
                              :agent/mbox_sha1sum
                              :agent/openid
                              :agent/account
                              :group/mbox
                              :group/mbox_sha1sum
                              :group/openid
                              :group/account]))))

(s/def ::agent*
  (s/or
   :mbox
   (s/keys :req [:agent/objectType
                 :agent/mbox]
           :opt [:agent/name])
   :mbox_sha1sum
   (s/keys :req [:agent/objectType
                 :agent/mbox_sha1sum]
           :opt [:agent/name])
   :openid
   (s/keys :req [:agent/objectType
                 :agent/openid]
           :opt [:agent/name])
   :account
   (s/keys :req [:agent/objectType
                 :agent/account]
           :opt [:agent/name])))

(s/def ::agent
  (s/and
   (namespace-conformer "agent")
   ::agent*))

;; Group

(s/def :group/objectType
  #{"Group"})

(s/def :group/name
  string?)

(s/def :group/mbox
  ::mailto-iri)

(s/def :group/mbox_sha1sum
  ::sha1sum)

(s/def :group/openid
  ::openid)

(s/def :group/account
  ::account)

(s/def :group/member
  ;; Hack, would be (s/coll-of ::agent :kind vector? :into []) but that doesn't unform properly
  (s/coll-of ::agent :kind vector? :into [] :gen-max 3)
  #_(s/and vector?
         (s/conformer identity vec)
         (s/cat ::agent (s/* ::agent))))


(s/def ::identified-group
  (s/or
   :mbox
   (s/keys :req [:group/objectType
                 :group/mbox]
           :opt [:group/name
                 :group/member])
   :mbox_sha1sum
   (s/keys :req [:group/objectType
                 :group/mbox_sha1sum]
           :opt [:group/name])
   :openid
   (s/keys :req [:group/objectType
                 :group/openid]
           :opt [:group/name])
   :account
   (s/keys :req [:group/objectType
                 :group/account]
           :opt [:group/name])))

(s/def ::anonymous-group
  (s/and
   (s/keys :req [:group/objectType
                 :group/member]
           :opt [:group/name])
   #(-> % :group/member seq)))

(s/def ::group*
  (s/or :identified-group
        ::identified-group
        :anonymous-group
        ::anonymous-group
        ))

(s/def ::group
  (s/and
   (namespace-conformer "group")
   ::group*))

;; Actor

(s/def ::actor
  (s/or ::agent
        ::agent
        ::group
        ::group))

;; Verb

(s/def :verb/id ::iri)

(s/def :verb/display ::language-map)

(s/def ::verb*
  (s/keys :req [:verb/id]
          :opt [:verb/display]))

(s/def ::verb
  (s/and
   (namespace-conformer "verb")
   (s/keys :req [:verb/id]
           :opt [:verb/display])))

;; Result

(s/def :score/scaled
  (s/double-in :min -1.0 :max 1.0))

(s/def :score/raw
  number?)

(s/def :score/min
  number?)

(s/def :score/max
  number?)

(s/def :result/score*
  (s/and
   (s/keys :opt [:score/scaled
                 :score/raw
                 :score/min
                 :score/max])
   (fn [{:keys [score/raw score/min score/max]}]
     (if (or min raw max)
       (apply <= (filter identity [min raw max]))
       true))))

(s/def :result/score
  (s/and
   (namespace-conformer "score")
   :result/score*))

(s/def :result/success
  boolean?)

(s/def :result/completion
  boolean?)

(s/def :result/response
  string?)

(s/def :result/duration
  ::duration)

(s/def :result/extensions
  ::extensions)

(s/def ::result*
  (s/keys :opt [:result/score
                :result/success
                :result/completion
                :result/response
                :result/duration
                :result/extensions]))

(s/def ::result
  (s/and
   (namespace-conformer "result")
   ::result*))

;; Statement Ref

(s/def :statement-ref/id ::uuid)

(s/def :statement-ref/objectType
  #{"StatementRef"})

(s/def ::statement-ref*
  (s/keys :req [:statement-ref/id
                :statement-ref/objectType]))

(s/def ::statement-ref
  (s/and (namespace-conformer "statement-ref")
         ::statement-ref*))

;; Context

(s/def ::context-activities-array
  (s/coll-of ::activity :kind vector? :into [] :min-count 1)
  #_(s/and vector?
         not-empty
         (s/conformer identity vec)
         (s/cat ::activity (s/* ::activity))))

(s/def ::context-activities
  (s/or ::context-activities-array
        ::context-activities-array
        ::activity
        ::activity))

(s/def :contextActivities/parent
  ::context-activities)

(s/def :contextActivities/grouping
  ::context-activities)

(s/def :contextActivities/category
  ::context-activities)

(s/def :contextActivities/other
  ::context-activities)

(s/def :context/contextActivities*
  (s/keys :opt [:contextActivities/parent
                :contextActivities/grouping
                :contextActivities/category
                :contextActivities/other]))

(s/def :context/contextActivities
  (s/and
   (namespace-conformer "contextActivities")
   :context/contextActivities*))


(s/def :context/registration
  ::uuid)

(s/def :context/instructor
  ::actor)

(s/def :context/team
  ::group)

(s/def :context/revision
  string?)

(s/def :context/platform
  string?)

(s/def :context/language
  ::language-tag)

(s/def :context/statement
  ::statement-ref)

(s/def :context/extensions
  ::extensions)

(s/def ::context*
  (s/keys :opt [:context/registration
                :context/instructor
                :context/team
                :context/contextActivities
                :context/revision
                :context/platform
                :context/language
                :context/statement
                :context/extensions]))

(s/def ::context
  (s/and
   (namespace-conformer "context")
   ::context*))

;; Attachments

(s/def :attachment/usageType
  ::iri)

(s/def :attachment/display
  ::language-map)

(s/def :attachment/description
  ::language-map)

(s/def :attachment/contentType
  string?)

(s/def :attachment/length
  int?)

(s/def :attachment/sha2
  ::sha2)

(s/def :attachment/fileUrl
  ::irl)

(s/def ::file-attachment
  (s/keys :req [:attachment/usageType
                :attachment/display
                :attachment/contentType
                :attachment/length
                :attachment/sha2]
          :req-opt [:attachment/description
                    :attachment/fileUrl]))

(s/def ::url-attachment
  (s/keys :req [:attachment/usageType
                :attachment/display
                :attachment/contentType
                :attachment/length
                :attachment/sha2
                :attachment/fileUrl]
          :req-opt [:attachment/description]))

(s/def ::attachment*
  (s/or ::file-attachment
        ::file-attachment
        ::url-attachment
        ::url-attachment))

(s/def ::attachment
  (s/and
   (namespace-conformer "attachment")
   ::attachment*))

(s/def ::attachments
  (s/coll-of ::attachment :kind vector? :into [])
  #_(s/and vector?
         not-empty
         (s/conformer identity vec)
         (s/cat ::attachment (s/* ::attachment))))

;; Sub-statement

(s/def :sub-statement/actor
  ::actor)

(s/def :sub-statement/verb
  ::verb)

(s/def :sub-statement/object
  (s/or ::agent
        ::agent
        ::group
        ::group
        ::statement-ref
        ::statement-ref))

(s/def :sub-statement/result
  ::result)

(s/def :sub-statement/context
  ::context)

(s/def :sub-statement/attachments
  ::attachments)

(s/def :sub-statement/timestamp
  ::timestamp)

(s/def :sub-statement/objectType
  #{"SubStatement"})

(s/def ::sub-statement*
  (s/keys :req [:sub-statement/actor
                :sub-statement/verb
                :sub-statement/object
                :sub-statement/objectType]
          :opt [:sub-statement/result
                :sub-statement/context
                :sub-statement/attachments
                :sub-statement/timestamp]))

(s/def ::sub-statement
  (s/and
   (namespace-conformer "sub-statement")
   ::sub-statement*))

;; Authority

(s/def ::oauth-consumer
  (s/merge ::agent
           (s/keys :req [:agent/account])))

(comment
  ;; three-legged oauth grp
  ;; FIXME: express these extra constraints
  (s/and :group/member
         (s/coll-of ::agent
                    :kind vector?
                    :count 2)
         (s/cat
          :oauth-consumer
          ::oauth-consumer
          :agent
          ::agent)))

(s/def ::three-legged-oauth-group*
  (s/keys :req [:group/objectType
                :group/member]
          :opt [:group/name
                :group/mbox
                :group/mbox_sha1sum
                :group/openid
                :group/account]))

(s/def ::three-legged-oauth-group
  (s/and
   (namespace-conformer "group")
   ::three-legged-oauth-group*))

;; Statement!

(s/def :statement/authority
  (s/or ::agent
        ::agent
        ::group
        ::three-legged-oauth-group))

(s/def :statement/object
  (s/or
   ::activity ::activity
   ::agent ::agent
   ::group ::group
   ::sub-statement ::sub-statement
   ::statement-ref ::statement-ref))

(s/def :statement/id
  ::uuid)

(s/def :statement/actor
  ::actor)

(s/def :statement/verb
  ::verb)

(s/def :statement/result
  ::result)

(s/def :statement/context
  ::context)

(s/def :statement/timestamp
  ::timestamp)

(s/def :statement/stored
  ::timestamp)

(s/def :statement/version
  ::version)

(s/def :statement/attachments
  ::attachments)

(s/def :statement/objectType
  #{"SubStatement"})

(def revision-or-platform?
  (some-fn :context/revision
           :context/platform))

(s/def ::statement*
  (s/and (s/keys :req [:statement/actor
                       :statement/verb
                       :statement/object]
                 :opt [:statement/id
                       :statement/result
                       :statement/context
                       :statement/timestamp
                       :statement/stored
                       :statement/authority
                       :statement/attachments
                       :statement/objectType])
         (fn valid-context? [s]
           (if (some-> s
                       :statement/object
                       first
                       (= ::activity))
             true
             (not (some-> s :statement/context revision-or-platform?))))
         (fn valid-void? [s]
           (if (some-> s :statement/verb :verb/id (= "http://adlnet.gov/expapi/verbs/voided"))
             (some-> s :statement/object first (= ::statement-ref))
             true))))

(s/def ::statement
  (s/and (namespace-conformer "statement")
         ::statement*))

(s/def ::statements
  (s/coll-of ::statement :kind vector? :into [] :min-count 1))
