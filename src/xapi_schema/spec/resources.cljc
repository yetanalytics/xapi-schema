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

;; Common

(s/def :xapi.common.param/agent
  (json
    (s/nonconforming
     ;; except for statement queries, groups are not allowed as agent params
     ::xs/agent)))

;; Statements
;; GET https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements

(s/def :xapi.statements.GET.request.params/statementId
  :statement/id)

(s/def :xapi.statements.GET.request.params/voidedStatementId
  :statement/id)

(s/def :xapi.statements.GET.request.params/agent
  (json
   (s/nonconforming
    ;; anonymous groups are not allowed as agent params
    ;; identified gorups, on the other hand, are allowed
    (s/or :agent ::xs/agent
          :group ::xs/identified-group))))

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
   (s/int-in 0 #?(:clj Long/MAX_VALUE
                  :cljs (.-MAX_VALUE js/Number)))))

(s/def :xapi.statements.GET.request.params/format
  #{"ids" "exact" "canonical"})

(s/def :xapi.statements.GET.request.params/attachments
  (json boolean?))

(s/def :xapi.statements.GET.request.params/ascending
  (json boolean?))

;; Putting it all together

(def singular-query?
  (comp
   some?
   (some-fn :statementId
            :voidedStatementId)))

(defmulti query-type
  #(if (singular-query? %)
     :xapi.statements.GET.request.params/singular
     :xapi.statements.GET.request.params/multiple))

(def statements-query-singular-spec
  (s/keys :req-un [(or :xapi.statements.GET.request.params/statementId
                       :xapi.statements.GET.request.params/voidedStatementId)]
          :opt-un [:xapi.statements.GET.request.params/format
                   :xapi.statements.GET.request.params/attachments]))

(defmethod query-type :xapi.statements.GET.request.params/singular [_]
  (s/with-gen statements-query-singular-spec
    ;; spec generates these with both of the required keys, which is weird.
    ;; Force it to be only one!
    (fn []
      (sgen/fmap (fn [params]
                   (dissoc params
                           (rand-nth [:statementId :voidedStatementId])))
                 (s/gen statements-query-singular-spec)))))

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

(s/def :xapi.statements.GET.response.statement-result/more
  ::xs/relative-irl)

(s/def :xapi.statements.GET.response/statement-result
  (s/keys :req-un [:xapi.statements.GET.response.statement-result/statements]
          :opt-un [:xapi.statements.GET.response.statement-result/more]))

;; Document Resources https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#22-document-resources

(def document-id
  (s/with-gen (s/and string?
                     not-empty)
    (fn []
      (sgen/not-empty
       (sgen/string-ascii)))))

;; ID
(s/def :xapi.document.params/stateId
  document-id)

(s/def :xapi.document.params/profileId
  document-id)

;; Context
(s/def :xapi.document.params/activityId
  :activity/id)

(s/def :xapi.document.params/agent
  :xapi.common.param/agent)

(s/def :xapi.document.params/registration
  ::xs/uuid)

;; Query

(s/def :xapi.document.params/since
  ::xs/timestamp)

;; State https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#23-state-resource

(s/def :xapi.document.state/context-params
  (s/keys :req-un [:xapi.document.params/activityId
                   :xapi.document.params/agent]
          :opt-un [:xapi.document.params/registration]))

;; Params for methods that work on a single state doc
(s/def :xapi.document.state/id-params
  (s/keys :req-un [:xapi.document.params/stateId
                   :xapi.document.params/activityId
                   :xapi.document.params/agent]
          :opt-un [:xapi.document.params/registration]))

(s/def :xapi.document.state/query-params
  (s/keys :req-un [:xapi.document.params/activityId
                   :xapi.document.params/agent]
          :opt-un [:xapi.document.params/registration
                   :xapi.document.params/since]))

;; Routes + Methods

(s/def :xapi.activities.state.PUT.request/params
  :xapi.document.state/id-params)

(s/def :xapi.activities.state.POST.request/params
  :xapi.document.state/id-params)

(s/def :xapi.activities.state.GET.request/params
  (s/or
   :id
   :xapi.document.state/id-params
   :query
   :xapi.document.state/query-params))

(s/def :xapi.activities.state.DELETE.request/params
  (s/or
   :id
   :xapi.document.state/id-params
   :context
   :xapi.document.state/context-params))

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
                      :xapi.agents.GET.response.person/account])
     walk/keywordize-keys
     walk/stringify-keys)))

;; Activities Resource https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource

(s/def :xapi.activities.GET.request.params/activityId
  ::xs/iri)

(s/def :xapi.activities.GET.request/params
  (s/keys :req-un [:xapi.activities.GET.request.params/activityId]))

;; Agent Profile https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#26-agent-profile-resource

(s/def :xapi.document.agent-profile/context-params
  (s/keys :req-un [:xapi.document.params/agent]))

;; Params for methods that work on a single state doc
(s/def :xapi.document.agent-profile/id-params
  (s/keys :req-un [:xapi.document.params/agent
                   :xapi.document.params/profileId]))

(s/def :xapi.document.agent-profile/query-params
  (s/keys :req-un [:xapi.document.params/agent]
          :opt-un [:xapi.document.params/since]))

;; Routes + Methods

(s/def :xapi.agents.profile.PUT.request/params
  :xapi.document.agent-profile/id-params)

(s/def :xapi.agents.profile.POST.request/params
  :xapi.document.agent-profile/id-params)

(s/def :xapi.agents.profile.GET.request/params
  (s/or
   :id
   :xapi.document.agent-profile/id-params
   :query
   :xapi.document.agent-profile/query-params))

(s/def :xapi.agents.profile.DELETE.request/params
  :xapi.document.agent-profile/id-params)

;; Activity Profile https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource

(s/def :xapi.document.activity-profile/context-params
  (s/keys :req-un [:xapi.document.params/activityId]))

;; Params for methods that work on a single activity-profile doc
(s/def :xapi.document.activity-profile/id-params
  (s/keys :req-un [:xapi.document.params/activityId
                   :xapi.document.params/profileId]))

(s/def :xapi.document.activity-profile/query-params
  (s/keys
   :req-un [:xapi.document.params/activityId]
   :opt-un [:xapi.document.params/since]))

;; Routes + Methods

(s/def :xapi.activities.profile.PUT.request/params
  :xapi.document.activity-profile/id-params)

(s/def :xapi.activities.profile.POST.request/params
  :xapi.document.activity-profile/id-params)

(s/def :xapi.activities.profile.GET.request/params
  (s/or
   :id
   :xapi.document.activity-profile/id-params
   :query
   :xapi.document.activity-profile/query-params))

(s/def :xapi.activities.profile.DELETE.request/params
  :xapi.document.activity-profile/id-params)

;; Abstract Document Params
;; useful for conforming
(s/def :xapi.document.generic/params
  (s/or :state
        (s/or :id
              :xapi.document.state/id-params
              :context
              :xapi.document.state/context-params
              :query
              :xapi.document.state/query-params)
        :agent-profile
        (s/or :id
              :xapi.document.agent-profile/id-params
              :context
              :xapi.document.agent-profile/context-params
              :query
              :xapi.document.agent-profile/query-params)
        :activity-profile
        (s/or :id
              :xapi.document.activity-profile/id-params
              :context
              :xapi.document.activity-profile/context-params
              :query
              :xapi.document.activity-profile/query-params)))

;; About https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#28-about-resource

(s/def :xapi.about.GET.response.body/version
  (s/coll-of ::xs/version :kind vector? :into []))

(s/def :xapi.about.GET.response.body/extensions
  ::xs/extensions)

(s/def :xapi.about.GET.response/body
  (s/keys :req-un [:xapi.about.GET.response.body/version]
          :opt-un [:xapi.about.GET.response.body/extensions]))
