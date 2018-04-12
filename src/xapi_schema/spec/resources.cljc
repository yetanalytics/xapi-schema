(ns xapi-schema.spec.resources
  (:require
   [xapi-schema.spec :as xs :include-macros true]
   [xapi-schema.spec.util :as util :include-macros true]
   [clojure.spec.alpha :as s #?@(:cljs [:include-macros true])]
   [clojure.spec.gen.alpha :as sgen :include-macros true]
   [clojure.walk :as walk]
   #?@(:clj [[clojure.data.json :as json]]))
  #?(:cljs (:require-macros [xapi-schema.spec.resources :refer [json]])))

(def ^:dynamic *read-json-fn*
  #?(:clj json/read-str
     :cljs (fn [s] (js->clj (.parse js/JSON s)))))

(def ^:dynamic *write-json-fn*
  #?(:clj json/write-str
     :cljs (fn [data] (.stringify js/JSON (clj->js data)))))

#?(:clj
   (defmacro with-json
     "Bind alternative json read/write fns"
     [{:keys [read-fn write-fn]
       :or {read-fn *read-json-fn*
            write-fn *write-json-fn*}} & body]
     `(binding [*read-json-fn* ~read-fn
                *write-json-fn* ~write-fn]
        ~@body)))

(defn conform-json [s]
  (if (string? s)
    (if (not-empty s)
      (try (*read-json-fn* ^String s)
           (catch #?(:clj java.lang.Exception
                     :cljs js/Error) _
             ::s/invalid))
      ::s/invalid)
    s))

(defn unform-json ^String [data]
  (if (string? data)
    data
    (try (*write-json-fn* data)
         (catch #?(:clj java.lang.Exception
                   :cljs js/Error) _
           ::s/invalid))))

(def json-string-conformer
  (s/conformer
   conform-json
   unform-json))

#?(:clj
   (defmacro json
     [spec]
     `(util/with-conformer
        ~spec conform-json unform-json)))

;; xAPI Resources

;; common
(s/def :xapi.common.param/agent
  (json
    (s/nonconforming ::xs/actor)))

;; Statements
;; GET https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
(s/def :xapi.statements.GET.request.params/statementId
  :statement/id)

(s/def :xapi.statements.GET.request.params/voidedStatementId
  :statement/id)

(s/def :xapi.statements.GET.request.params/agent
  :xapi.common.param/agent)

(s/def :xapi.statements.GET.request.params/verb
  ::xs/iri)

(s/def :xapi.statements.GET.request.params/activity
  ::xs/iri)

(s/def :xapi.statements.GET.request.params/registration
  ::xs/uuid)

(s/def :xapi.statements.GET.request.params/related_activities
  (json boolean?))

(s/def :xapi.statements.GET.request.params/related_agents
  (json boolean?))

(s/def :xapi.statements.GET.request.params/since
  ::xs/timestamp)

(s/def :xapi.statements.GET.request.params/until
  ::xs/timestamp)

(s/def :xapi.statements.GET.request.params/limit
  (json
    (complement neg-int?)))

(s/def :xapi.statements.GET.request.params/format
  #{"ids" "exact" "canonical"})

(s/def :xapi.statements.GET.request.params/attachments
  (json boolean?))

(s/def :xapi.statements.GET.request.params/ascending
  (json boolean?))

;;

(def singular-query?
  (comp
   some?
   (some-fn :statementId
            :voidedStatementId)))

(defmulti query-type
  #(if (singular-query? %)
     :xapi.statements.GET.request.params/singular
     :xapi.statements.GET.request.params/multiple))

(defmethod query-type :xapi.statements.GET.request.params/singular [_]
  (s/keys :req-un [(or :xapi.statements.GET.request.params/statementId
                       :xapi.statements.GET.request.params/voidedStatementId)]
          :opt-un [:xapi.statements.GET.request.params/format
                   :xapi.statements.GET.request.params/attachments]))

(defmethod query-type :xapi.statements.GET.request.params/multiple [_]
  (s/keys :opt-un [:xapi.statements.GET.request.params/agent
                   :xapi.statements.GET.request.params/verb
                   :xapi.statements.GET.request.params/activity
                   :xapi.statements.GET.request.params/registration
                   :xapi.statements.GET.request.params/related_activities
                   :xapi.statements.GET.request.params/related_agents
                   :xapi.statements.GET.request.params/since
                   :xapi.statements.GET.request.params/until
                   :xapi.statements.GET.request.params/limit
                   :xapi.statements.GET.request.params/format
                   :xapi.statements.GET.request.params/attachments
                   :xapi.statements.GET.request.params/ascending]))


(s/def :xapi.statements.GET.request/params
  (s/multi-spec query-type (fn [gen-val _]
                             gen-val)))
(s/def :xapi.statements.PUT.request.params/statementId
  :statement/id)

(s/def :xapi.statements.PUT.request/params
  (s/keys :req-un [:xapi.statements.PUT.request.params/statementId]))

;; StatementResult https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#retrieval
(s/def :xapi.statements.GET.response.statement-result/statements
  (s/coll-of (s/nonconforming ::xs/statement) :into []))

(s/def :xapi.statements.GET.response.statment-result/more
  ::xs/relative-irl)

(s/def :xapi.statements.GET.response/statement-result
  (s/keys :req-un [:xapi.statements.GET.response.statement-result/statements]
          :opt-un [:xapi.statements.GET.response.statement-result/more]))

;; Document Resources https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#22-document-resources
(s/def :xapi.document.generic/id
  (s/and string?
         not-empty))

(s/def :xapi.document.generic/updated
  ::xs/timestamp)

(s/def :xapi.document.generic/contents
  (s/with-gen identity
    (constantly sgen/any)))

(s/def :xapi.document/generic
  (s/keys :req-un [:xapi.document.generic/id
                   :xapi.document.generic/updated
                   :xapi.document.generic/contents]))

;; State https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#23-state-resource

(s/def :xapi.activities.state.*.request.params/activityId
  ::xs/iri)

(s/def :xapi.activities.state.*.request.params/agent
  :xapi.common.param/agent)

(s/def :xapi.activities.state.*.request.params/registration
  ::xs/uuid)

(s/def :xapi.activities.state.*.request.params/stateId
  (s/and string?
         not-empty))

(s/def :xapi.activities.state.*.request.params/since
  (s/and string?
         ::xs/since))

(s/def :xapi.activities.state.*.request.singular/params
  (s/keys :req-un [:xapi.activities.state.*.request.params/activityId
                   :xapi.activities.state.*.request.params/agent]
          :opt-un [:xapi.activities.state.*.request.params/registration
                   :xapi.activities.state.*.request.params/stateId]))
;; For multiple GET
(s/def :xapi.activities.state.GET.request.multiple/params
  (s/keys :req-un [:xapi.activities.state.*.request.params/activityId
                   :xapi.activities.state.*.request.params/agent]
          :opt-un [:xapi.activities.state.*.request.params/registration
                   :xapi.activities.state.*.request.params/since]))

(s/def :xapi.activities.state.DELETE.request.multiple/params
  (s/keys :req-un [:xapi.activities.state.*.request.params/activityId
                   :xapi.activities.state.*.request.params/agent]
          :opt-un [:xapi.activities.state.*.request.params/registration]))

;; Agents https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#24-agents-resource
(s/def :xapi.agents.GET.request.params/agent
  :xapi.common.param/agent)

(s/def :xapi.agents.GET.request/params
  (s/keys :req-un [:xapi.agents.GET.request.params/agent]))

;; Person https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#person-properties
(s/def :xapi.agents.GET.response.person/objectType
  #{"Person"})

(s/def :xapi.agents.GET.response.person/name
  (s/coll-of string? :kind vector? :into []))

(s/def :xapi.agents.GET.response.person/mbox
  (s/coll-of ::xs/mailto-iri :kind vector? :into []))

(s/def :xapi.agents.GET.response.person/mbox_sha1sum
  (s/coll-of ::xs/sha1sum :kind vector? :into []))

(s/def :xapi.agents.GET.response.person/openid
  (s/coll-of ::xs/openid :kind vector? :into []))

(s/def :xapi.agents.GET.response.person/account
  (s/coll-of ::xs/account :kind vector? :into []))

(s/def :xapi.agents.GET.response/person
  (s/nonconforming
   (util/with-conformer
     (s/keys :req-un [:xapi.agents.GET.response.person/objectType]
             :opt-un [:xapi.agents.GET.response.person/name
                      :xapi.agents.GET.response.person/mbox
                      :xapi.agents.GET.response.person/mbox_sha1sum
                      :xapi.agents.GET.response.person/openid
                      :xapi.agents.GET.response.person/account
                      ])
     walk/keywordize-keys
     walk/stringify-keys)))

;; Activities Resource https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource

(s/def :xapi.activities.GET.request.params/activityId
  ::xs/iri)

(s/def :xapi.activities.GET.request/params
  (s/keys :req-un [:xapi.activities.GET.request.params/activityId]))

;; Agent Profile https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#26-agent-profile-resource

(s/def :xapi.agents.profile.*.request.params/agent
  :xapi.common.param/agent)

(s/def :xapi.agents.profile.*.request.params/profileId
  (s/and string?
         not-empty))

(s/def :xapi.agents.profile.*.request.params/since
  ::xs/timestamp)

(s/def :xapi.agents.profile.*.request.singular/params
  (s/keys :req-un [:xapi.agents.profile.*.request.params/agent
                   :xapi.agents.profile.*.request.params/profileId]))

(s/def :xapi.agents.profile.GET.request.multiple/params
  (s/keys :req-un [:xapi.agents.profile.*.request.params/agent]
          :opt-un [:xapi.agents.profile.*.request.params/since]))

;; Activity Profile https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource

(s/def :xapi.activities.profile.*.request.params/activityId
  ::xs/iri)

(s/def :xapi.activities.profile.*.request.params/profileId
  (s/and string?
         not-empty))

(s/def :xapi.activities.profile.*.request.params/since
  ::xs/timestamp)

(s/def :xapi.activities.profile.*.request.singular/params
  (s/keys :req-un [:xapi.activities.profile.*.request.params/activityId
                   :xapi.activities.profile.*.request.params/profileId]))

(s/def :xapi.activities.profile.GET.request.multiple/params
  (s/keys :req-un [:xapi.activities.profile.*.request.params/activityId]
          :opt-un [:xapi.activities.profile.*.request.params/since]))

;; About https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#28-about-resource

(s/def :xapi.about.GET.response.body/version
  (s/coll-of ::xs/version :kind vector? :into []))

(s/def :xapi.about.GET.response.body/extensions
  ::xs/extensions)

(s/def :xapi.about.GET.response/body
  (s/keys :req-un [:xapi.about.GET.response.body/version]
          :opt-un [:xapi.about.GET.response.body/extensions]))
