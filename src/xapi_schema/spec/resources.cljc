(ns xapi-schema.spec.resources
  (:require
   [xapi-schema.spec :as xs :refer [conform-ns restrict-keys]]
   [clojure.spec.alpha :as s #?@(:cljs [:include-macros true])]
   [clojure.spec.gen.alpha :as sgen :include-macros true]
   #?@(:clj [[cheshire.core :as json]]))
  #?(:cljs (:require-macros [xapi-schema.spec :refer [conform-ns]])))

(defn parse-json [^String s]
  #?(:clj (json/parse-string-strict s)
     :cljs (js->clj (.parse js/JSON s))))

(defn unparse-json [data]
  #?(:clj (json/generate-string data)
     :cljs (.stringify js/JSON (clj->js data))))

(def json-string-conformer
  (s/conformer (fn [s]
                 (if (string? s)
                   (if (not-empty s)
                     (try (parse-json s)
                          (catch #?(:clj java.lang.Exception
                                    :cljs js/Error) _
                            ::s/invalid))
                     ::s/invalid)
                   s))
               (fn [data]
                 (if (string? data)
                   data
                   (try (unparse-json data)
                        (catch #?(:clj java.lang.Exception
                                  :cljs js/Error) _
                          ::s/invalid))))))

;; xAPI Resources

;; common
(s/def :xapi.common.param/agent
  (s/with-gen (s/and json-string-conformer
                     ::xs/actor)
    #(sgen/fmap unparse-json (s/gen ::xs/actor))))

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
  boolean?)

(s/def :xapi.statements.GET.request.params/related_agents
  boolean?)

(s/def :xapi.statements.GET.request.params/since
  ::xs/timestamp)

(s/def :xapi.statements.GET.request.params/until
  ::xs/timestamp)

(s/def :xapi.statements.GET.request.params/limit
  (complement neg-int?))

(s/def :xapi.statements.GET.request.params/format
  #{"ids" "exact" "canonical"})

(s/def :xapi.statements.GET.request.params/attachments
  boolean?)

(s/def :xapi.statements.GET.request.params/ascending
  boolean?)

;;

(def singular-query?
  (comp
   some?
   (some-fn :xapi.statements.GET.request.params/statementId
            :xapi.statements.GET.request.params/voidedStatementId)))

(defmulti query-type
  #(if (singular-query? %)
     :xapi.statements.GET.request.params/singular
     :xapi.statements.GET.request.params/multiple))

(defmethod query-type :xapi.statements.GET.request.params/singular [_]
  (s/and (s/keys :req [(or :xapi.statements.GET.request.params/statementId
                           :xapi.statements.GET.request.params/voidedStatementId)]
                 :opt [:xapi.statements.GET.request.params/format
                       :xapi.statements.GET.request.params/attachments])
         (restrict-keys :xapi.statements.GET.request.params/statementId
                        :xapi.statements.GET.request.params/voidedStatementId
                        :xapi.statements.GET.request.params/format
                        :xapi.statements.GET.request.params/attachments)))

(defmethod query-type :xapi.statements.GET.request.params/multiple [_]
  (s/and (s/keys :opt [:xapi.statements.GET.request.params/agent
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
                       :xapi.statements.GET.request.params/ascending])
         (restrict-keys
          :xapi.statements.GET.request.params/agent
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
          :xapi.statements.GET.request.params/ascending)))


(s/def :xapi.statements.GET.request/params
  (conform-ns "xapi.statements.GET.request.params"
              (s/multi-spec query-type (fn [gen-val _]
                                         gen-val))))


;; StatementResult https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#retrieval
(s/def :xapi.statements.GET.response.statement-result/statements
  (s/coll-of ::xs/statement :into []))

(s/def :xapi.statements.GET.response.statment-result/more
  ::xs/relative-irl)

(s/def :xapi.statements.GET.response/statement-result
  (conform-ns "xapi.statements.GET.response.statement-result"
              (s/and
               (s/keys :req [:xapi.statements.GET.response.statement-result/statements]
                       :opt [:xapi.statements.GET.response.statement-result/more])
               (restrict-keys
                :xapi.statements.GET.response.statement-result/statements
                :xapi.statements.GET.response.statement-result/more))))

;; Document Resources https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#22-document-resources
(s/def :xapi.document.generic/id
  (s/and string?
         not-empty))

(s/def :xapi.document.generic/updated
  ::xs/timestamp)

(s/def :xapi.document.generic/contents
  (s/with-gen identity
    (constantly sgen/any)))

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
  (conform-ns "xapi.activities.state.*.request.params"
              (s/keys :req [:xapi.activities.state.*.request.params/activityId
                            :xapi.activities.state.*.request.params/agent]
                      :opt [:xapi.activities.state.*.request.params/registration
                            :xapi.activities.state.*.request.params/stateId])))
;; For multiple GET
(s/def :xapi.activities.state.GET.request.multiple/params
  (conform-ns "xapi.activities.state.*.request.params"
              (s/keys :req [:xapi.activities.state.*.request.params/activityId
                            :xapi.activities.state.*.request.params/agent]
                      :opt [:xapi.activities.state.*.request.params/registration
                            :xapi.activities.state.*.request.params/since])))

(s/def :xapi.activities.state.DELETE.request.multiple/params
  (conform-ns "xapi.activities.state.*.request.params"
              (s/keys :req [:xapi.activities.state.*.request.params/activityId
                            :xapi.activities.state.*.request.params/agent]
                      :opt [:xapi.activities.state.*.request.params/registration])))

;; Agents https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#24-agents-resource
(s/def :xapi.agents.GET.request.params/agent
  :xapi.common.param/agent)

(s/def :xapi.agents.GET.request/params
  (conform-ns "xapi.agents.GET.request.params"
              (s/keys :req [:xapi.agents.GET.request.params/agent])))

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
  (conform-ns "xapi.agents.GET.response.person"
              (s/and
               (s/keys :req [:xapi.agents.GET.response.person/objectType]
                       :opt [:xapi.agents.GET.response.person/name
                             :xapi.agents.GET.response.person/mbox
                             :xapi.agents.GET.response.person/mbox_sha1sum
                             :xapi.agents.GET.response.person/openid
                             :xapi.agents.GET.response.person/account
                             ])
               (restrict-keys :xapi.agents.GET.response.person/name
                              :xapi.agents.GET.response.person/mbox
                              :xapi.agents.GET.response.person/mbox_sha1sum
                              :xapi.agents.GET.response.person/openid
                              :xapi.agents.GET.response.person/account
                              :xapi.agents.GET.response.person/objectType
                              ))))

;; Activities Resource https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource

(s/def :xapi.activities.GET.request.params/activityId
  ::xs/iri)

(s/def :xapi.activities.GET.request/params
  (conform-ns "xapi.activities.GET.request.params"
              (s/keys :req [:xapi.activities.GET.request.params/activityId])))

;; Agent Profile https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#26-agent-profile-resource

(s/def :xapi.agents.profile.*.request.params/agent
  :xapi.common.param/agent)

(s/def :xapi.agents.profile.*.request.params/profileId
  (s/and string?
         not-empty))

(s/def :xapi.agents.profile.*.request.params/since
  ::xs/timestamp)

(s/def :xapi.agents.profile.*.request.singular/params
  (conform-ns "xapi.agents.profile.*.request.params"
              (s/keys :req [:xapi.agents.profile.*.request.params/agent
                            :xapi.agents.profile.*.request.params/profileId])))

(s/def :xapi.agents.profile.GET.request.multiple/params
  (conform-ns "xapi.agents.profile.*.request.params"
              (s/keys :req [:xapi.agents.profile.*.request.params/agent]
                      :opt [:xapi.agents.profile.*.request.params/since])))

;; Activity Profile https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource

(s/def :xapi.activities.profile.*.request.params/activityId
  ::xs/iri)

(s/def :xapi.activities.profile.*.request.params/profileId
  (s/and string?
         not-empty))

(s/def :xapi.activities.profile.*.request.params/since
  ::xs/timestamp)

(s/def :xapi.activities.profile.*.request.singular/params
  (conform-ns "xapi.activities.profile.*.request.params"
              (s/keys :req [:xapi.activities.profile.*.request.params/activityId
                            :xapi.activities.profile.*.request.params/profileId])))

(s/def :xapi.activities.profile.GET.request.multiple/params
  (conform-ns "xapi.activities.profile.*.request.params"
              (s/keys :req [:xapi.activities.profile.*.request.params/activityId]
                      :opt [:xapi.activities.profile.*.request.params/since])))

;; About https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#28-about-resource

(s/def :xapi.about.GET.response.body/version
  (s/coll-of ::xs/version :kind vector? :into []))

(s/def :xapi.about.GET.response.body/extensions
  ::xs/extensions)

(s/def :xapi.about.GET.response/body
  (conform-ns "xapi.about.GET.response.body"
              (s/keys :req [:xapi.about.GET.response.body/version]
                      :opt [:xapi.about.GET.response.body/extensions])))
