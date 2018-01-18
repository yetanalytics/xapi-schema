(ns xapi-schema.support.gen
  (:require
   [clojure.spec.alpha :as s :include-macros true]
   [xapi-schema.spec :as xapispec]
   [com.gfredericks.test.chuck :as chuck]
   [com.gfredericks.test.chuck.generators :as cgen]
   [xapi-schema.schemata.regex :as re]
   [clojure.test.check.generators :as gen :include-macros true]
   ;; [clojure.test.check :as tc]
   ;; [clojure.test.check.properties :as prop]
   [#?(:clj clj-time.format
       :cljs cljs-time.format) :as tf]))

(defn remove-re-anchors
  "Remove regex anchors so test.chuck works"
  ^#?(:clj java.util.regex.Pattern
     :cljs js/RegExp) [^#?(:clj java.util.regex.Pattern
                           :cljs js/RegExp) p]
  (let [ps (str p)
        psc (count ps)]
    (re-pattern (subs
                 ps
                 (if (= \^ (first ps))
                   1
                   0)
                 (cond-> psc
                   (= \$ (last ps)) dec)))))

#_(defn ns-conform?
  "Does a spec use a namespace conformer?"
  [spec-kw]
  (some-> (s/form spec-kw) second first (= 'xapi-schema.spec/map-ns-conformer)))

#_(defn spec-conform-ns
  "Get a spec's conform ns, if present"
  [spec-kw]
  (-> (s/form spec-kw) second second))

(def primitive-overrides
  {::xapispec/string-not-empty
   #(gen/not-empty gen/string-alphanumeric)
   ::xapispec/language-tag
   #?(:clj #(cgen/string-from-regex (remove-re-anchors re/LanguageTagRegEx))
      :cljs #(gen/elements ["en" "en-US" "en-GB" "fr"]))
   ::xapispec/iri
   #?(:clj #(cgen/string-from-regex (remove-re-anchors re/AbsoluteIRIRegEx))
      :cljs #(gen/let [protocol (gen/not-empty gen/string-alphanumeric)
                       host (gen/not-empty gen/string-alphanumeric)
                       domain (gen/not-empty gen/string-alphanumeric)
                       tld (gen/not-empty gen/string-alphanumeric)
                       path (gen/not-empty gen/string-alphanumeric)]
               (str protocol "://" host "." domain "." tld "/" path)))
   ::xapispec/mailto-iri
   #?(:clj #(cgen/string-from-regex re/MailToIRIRegEx)
      :cljs #(gen/let [mbox (gen/not-empty gen/string-alphanumeric)
                       host (gen/not-empty gen/string-alphanumeric)
                       domain (gen/not-empty gen/string-alphanumeric)
                       tld (gen/not-empty gen/string-alphanumeric)]
               (str "mailto:" mbox "@" host "." domain "." tld)))
   ::xapispec/irl
   #?(:clj #(cgen/string-from-regex (remove-re-anchors re/AbsoluteIRIRegEx))
      :cljs #(gen/let [protocol (gen/not-empty gen/string-alphanumeric)
                       host (gen/not-empty gen/string-alphanumeric)
                       domain (gen/not-empty gen/string-alphanumeric)
                       tld (gen/not-empty gen/string-alphanumeric)
                       path (gen/not-empty gen/string-alphanumeric)]
               (str protocol "://" host "." domain "." tld "/" path)))
   ::xapispec/openid
   #?(:clj #(cgen/string-from-regex (remove-re-anchors re/OpenIdRegEx))
      :cljs #(gen/let [host (gen/not-empty gen/string-alphanumeric)
                       domain (gen/not-empty gen/string-alphanumeric)
                       tld (gen/not-empty gen/string-alphanumeric)
                       path (gen/not-empty gen/string-alphanumeric)]
               (str "https://" host "." domain "." tld "/" path)))
   ::xapispec/uuid
   #?(:clj #(cgen/string-from-regex re/UuidRegEx)
      :cljs #(gen/fmap str gen/uuid))
   ::xapispec/timestamp
   #(gen/fmap (fn [dt]
                (tf/unparse (tf/formatters :date-time) dt))
              (cgen/datetime))
   ::xapispec/duration
   #(gen/return "P23DT23H") ;; TODO: dynamic
   ::xapispec/version
   #?(:clj #(cgen/string-from-regex (remove-re-anchors re/xAPIVersionRegEx))
      :cljs #(gen/let [r gen/pos-int]
               (str "1.0." r)))

   ::xapispec/sha2
   #?(:clj #(cgen/string-from-regex (remove-re-anchors re/Base64RegEx))
      :cljs #(gen/let [rs (gen/not-empty gen/string-alphanumeric)]
               (.btoa js/window rs)))
   ::xapispec/sha1sum
   #?(:clj #(cgen/string-from-regex (remove-re-anchors re/Sha1RegEx))
      :cljs #(gen/elements ["Fba13C82BED3586AAffcC1254f7dbE0cfBD7568E"]))}) ;; todo, make actually random

(defn unform-gen [spec-kw overrides & [select-map-keys]]
  (let [[kns kname] ((juxt namespace name)
                     spec-kw)]
    (gen/fmap (cond-> xapispec/unform-ns-map
                select-map-keys (comp
                                 #(select-keys
                                   %
                                   select-map-keys)))
              (gen/not-empty
               (s/gen (keyword kns (str kname "*")) overrides)))))


(def interaction-component-overrides
  {::xapispec/interaction-component #(unform-gen ::xapispec/interaction-component primitive-overrides)})

(def activity-definition-overrides
  {:activity/definition #(unform-gen :activity/definition (merge
                                                           primitive-overrides
                                                           interaction-component-overrides)
                                     [:definition/name
                                      :definition/description
                                      :definition/correctResponsesPattern
                                      :definition/interactionType
                                      :definition/type
                                      :definition/moreInfo
                                      :definition/extensions])})

(def activity-overrides
  {::xapispec/activity #(unform-gen ::xapispec/activity (merge
                                                         primitive-overrides
                                                         interaction-component-overrides
                                                         activity-definition-overrides))})

(def account-overrides
  {::xapispec/account
   #(unform-gen ::xapispec/account
                primitive-overrides)})

(defn rand-agent-ifi []
  (rand-nth [:agent/mbox
             :agent/mbox_sha1sum
             :agent/openid
             :agent/account]))

(def agent-overrides
  {::xapispec/agent
   #(unform-gen ::xapispec/agent
                (merge primitive-overrides
                       account-overrides)
                [:agent/objectType
                 :agent/name
                 (rand-agent-ifi)])})

(defn rand-group-ifi []
  (rand-nth [:group/mbox
             :group/mbox_sha1sum
             :group/openid
             :group/account]))

(def group-overrides
  {::xapispec/group
   #(unform-gen ::xapispec/group
                (merge primitive-overrides
                       account-overrides
                       agent-overrides)
                [:group/objectType
                 :group/member
                 :group/name
                 (rand-group-ifi)])})

(def verb-overrides
  {::xapispec/verb
   #(unform-gen ::xapispec/verb
                primitive-overrides)})

(def score-overrides
  {:result/score
   #(unform-gen :result/score
                primitive-overrides)})

(def result-overrides
  {::xapispec/result
   #(unform-gen ::xapispec/result
                (merge primitive-overrides
                       score-overrides))})

(def statement-ref-overrides
  {::xapispec/statement-ref
   #(unform-gen ::xapispec/statement-ref
                primitive-overrides)})

(def context-activities-overrides
  {:context/contextActivities
   #(unform-gen :context/contextActivities
                (merge primitive-overrides
                       activity-overrides))})

(def context-overrides
  {::xapispec/context
   #(unform-gen ::xapispec/context
                (merge primitive-overrides
                       context-activities-overrides
                       agent-overrides
                       group-overrides
                       statement-ref-overrides))})

(def attachment-overrides
  {::xapispec/attachment
   #(unform-gen ::xapispec/attachment
                primitive-overrides)})

(def sub-statement-overrides
  {::xapispec/sub-statement
   #(unform-gen ::xapispec/sub-statement
                (merge primitive-overrides
                       activity-overrides
                       agent-overrides
                       group-overrides
                       verb-overrides
                       result-overrides
                       statement-ref-overrides
                       context-overrides
                       attachment-overrides))})

(comment
  (def oauth-consumer-overrides
    {::xapispec/oauth-consumer
     #(unform-gen ::xapispec/oauth-consumer
                  (merge primitive-overrides
                         agent-overrides)
                  [:agent/account])})

  (def three-legged-oauth-group-overrides
    {::xapispec/three-legged-oauth-group
     #(unform-gen ::xapispec/three-legged-oauth-group
                  (merge primitive-overrides
                         group-overrides
                         oauth-consumer-overrides))})
)
(def statement-overrides
  {::xapispec/statement
   #(unform-gen ::xapispec/statement
                (merge primitive-overrides
                       activity-overrides
                       agent-overrides
                       group-overrides
                       verb-overrides
                       result-overrides
                       statement-ref-overrides
                       context-overrides
                       attachment-overrides
                       sub-statement-overrides))})

(def gen-overrides
  (merge primitive-overrides
         interaction-component-overrides
         activity-definition-overrides
         activity-overrides
         account-overrides
         agent-overrides
         group-overrides
         verb-overrides
         score-overrides
         result-overrides
         statement-ref-overrides
         context-activities-overrides
         context-overrides
         attachment-overrides
         sub-statement-overrides
         statement-overrides))

(defn spec-gen [spec-kw & [overrides]]
  (s/gen spec-kw
         (merge gen-overrides
                overrides)))
