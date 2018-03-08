(ns xapi-schema.spec
  (:require
   [xapi-schema.spec.regex :refer [LanguageTagRegEx
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
   [clojure.spec.alpha :as s #?@(:cljs [:include-macros true])]
   [clojure.spec.gen.alpha :as sgen :include-macros true]
   [clojure.string :as cstr]
   #?@(:cljs [[goog.string :as gstring]
              [goog.string.format]]))
  #?(:clj (:import [java.util Base64])
     :cljs (:require-macros [xapi-schema.spec :refer [conform-ns]])))

;; Utils

(def double-conformer
  (s/conformer (fn [n]
                 (try (double n)
                      (catch #?(:clj Exception
                                :cljs js/Error) e
                        ::s/invalid)))))

(defn conform-ns-map [map-ns string-map]
  (try (reduce-kv (fn [m k v]
                    (assoc m (if (string? k)
                               (keyword map-ns k)
                               k) v))
                  {}
                  string-map)
       (catch #?(:clj Exception
                 :cljs js/Error) e
         ::s/invalid)))

(defn unform-ns-map [keyword-map]
  (try (reduce-kv (fn [m k v]
                    (assoc m (if (qualified-keyword? k)
                               (name k)
                               k) v))
                  {}
                  keyword-map)
       (catch #?(:clj Exception
                 :cljs js/Error) e
         ::s/invalid)))

(defn map-ns-conformer
  [map-ns]
  (s/conformer
   (partial conform-ns-map map-ns)
   unform-ns-map))

#?(:clj (defmacro conform-ns
          [map-ns & spec-body]
          `(s/with-gen (s/and
                        (s/conformer
                         (partial conform-ns-map ~map-ns)
                         unform-ns-map)
                        ~@spec-body)
             #(sgen/fmap
               unform-ns-map
               (s/gen ~@spec-body)))))

(defn restrict-keys
  "Return a predicate that asserts that only the given keys are present."
  [& ks]
  (fn [m] (every? (set ks) (keys m))))

(def revision-or-platform?
  (comp some?
        (some-fn :context/revision
                 :context/platform)))

;; primitives - useful to hook into for generation
(s/def ::string-not-empty
  (s/with-gen
    (s/and string?
           (complement empty?))
    #(sgen/not-empty (sgen/string-alphanumeric))))

;; Leaves

(s/def ::language-tag
  (s/with-gen
    (s/and ::string-not-empty
           (partial re-matches LanguageTagRegEx))
    #(sgen/elements ["en" "en-US" "en-GB" "fr"])))

(s/def ::language-map
  (s/map-of ::language-tag
            ::string-not-empty
            :gen-max 3
            :min-count 1))

(defn into-str [cs]
  (cstr/lower-case (apply str cs)))

(s/def ::iri
  (s/with-gen
    (s/and string?
           (partial re-matches AbsoluteIRIRegEx))
    #(sgen/fmap
      (fn [[protocol host domain tld path]]
        (str protocol "://" host "." domain "." tld "/" path)) ;; TODO: dynamic protocol
      (sgen/tuple
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 8))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 10))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 10))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 4))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 16))))))

(s/def ::mailto-iri
  (s/with-gen
    (s/and string?
           (partial re-matches MailToIRIRegEx))
    #(sgen/fmap
      (fn [[mbox domain tld]]
        (str "mailto:" mbox "@" domain "." tld))
      (sgen/tuple (sgen/fmap into-str
                             (sgen/vector (sgen/char-alpha) 3 16))
                  (sgen/fmap into-str
                             (sgen/vector (sgen/char-alpha) 3 12))
                  (sgen/fmap into-str
                             (sgen/vector (sgen/char-alpha) 3 4))))))

(s/def ::irl
  (s/with-gen
    (s/and string?
           (partial re-matches AbsoluteIRIRegEx))
    #(sgen/fmap
      (fn [[protocol host domain tld path]]
        (str protocol "://" host "." domain "." tld "/" path)) ;; TODO: dynamic protocol
      (sgen/tuple
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 8))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 10))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 10))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 4))
       (sgen/fmap into-str
                  (sgen/vector (sgen/char-alpha) 3 16))))))

(s/def ::any-json
  (s/nilable
   (s/or :scalar
         (s/or :string
               string?
               :number
               (s/or :double
                     (s/double-in :infinite? false :NaN? false
                                  :max 1000.0 :min -1000.0)
                     :int
                     int?)
               :boolean
               boolean?)
         :coll
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
                :gen-max 4)))))

(s/def ::extensions
  (s/map-of ::iri
            ::any-json))

(s/def ::openid
  (s/with-gen
    (s/and string?
           (partial re-matches OpenIdRegEx))
    #(sgen/fmap
      (fn [[protocol host domain tld path]]
        (str protocol "://" host "." domain "." tld "/" path))
      (sgen/tuple (sgen/elements ["http" "https"])
                  (sgen/fmap into-str
                             (sgen/vector (sgen/char-alpha) 3 10))
                  (sgen/fmap into-str
                             (sgen/vector (sgen/char-alpha) 3 10))
                  (sgen/fmap into-str
                             (sgen/vector (sgen/char-alpha) 3 4))
                  (sgen/fmap into-str
                             (sgen/vector (sgen/char-alpha) 3 16))
                  ))))

(s/def ::uuid
  (s/with-gen
    (s/and string?
           (partial re-matches UuidRegEx))
    #(sgen/fmap
      str
      (sgen/uuid))))


(s/def ::timestamp
  (s/with-gen
    (s/and string?
           (partial re-matches TimestampRegEx))
    #(sgen/fmap (fn [[yyyy mm dd h m s ms]]
                  (#?(:clj format
                      :cljs gstring/format) "%d-%02d-%02dT%02d:%02d:%02d.%dZ" yyyy mm dd h m s ms))
               (sgen/tuple (sgen/elements (range 1970 2020))
                           (sgen/elements (range 1 12))
                           (sgen/elements (range 1 28))
                           (sgen/elements (range 0 24))
                           (sgen/elements (range 0 60))
                           (sgen/elements (range 0 60))
                           (sgen/elements (range 0 999))))))

(s/def ::duration
  (s/with-gen
    (s/and string?
           (partial re-matches DurationRegEx))
    #(sgen/fmap (fn [[h m s]]
                  (#?(:clj format
                      :cljs gstring/format) "PT%dH%sM%dS" h m s))
                (sgen/tuple (sgen/elements (range 1 24))
                            (sgen/elements (range 1 60))
                            (sgen/elements (range 1 60))))))

(s/def ::version
  (s/with-gen
    (s/and string?
           (partial re-matches xAPIVersionRegEx))
    #(sgen/fmap (fn [i]
                  (#?(:clj format
                      :cljs gstring/format) "1.0.%d" i))
                (sgen/int))))

(s/def ::sha2
  (s/with-gen
    (s/and string?
           (partial re-matches Base64RegEx))
    #(sgen/fmap
      (fn [^String s]
        #?(:clj (String. (.encode
                          (java.util.Base64/getEncoder)
                          (.getBytes s)))
           :cljs (js/btoa s)))
      (sgen/not-empty (sgen/string-alphanumeric)))))

(s/def ::sha1sum
  (s/with-gen
    (s/and string?
           (partial re-matches Sha1RegEx))
    #(sgen/fmap
      (fn [is]
        (apply str
               (map char
                    is)))
      (sgen/vector (sgen/elements (concat
                                   (range 65 71)
                                   (range 48 58)))
                40))))

;; Activity Definition

(s/def :interaction-component/id
  ::string-not-empty)

(s/def :interaction-component/description
  ::language-map)

(s/def ::interaction-component
  (conform-ns "interaction-component"
              (s/and
               (s/keys :req [:interaction-component/id]
                       :opt [:interaction-component/description])
               (restrict-keys :interaction-component/id
                              :interaction-component/description))))

(s/def ::interaction-components
  (s/with-gen
    (s/and (s/coll-of ::interaction-component :kind vector? :into [])
           (fn [icomps]
             (when (seq icomps)
               (apply distinct? (map (some-fn
                                      :interaction-component/id
                                      #(get % "id")) icomps)))))
    #(sgen/not-empty (sgen/vector-distinct (s/gen ::interaction-component)))))


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

(defmulti interaction-type :definition/interactionType)

(defmethod interaction-type "choice" [_]
  (s/and
   (s/keys
    :req [:definition/interactionType]
    :opt [:definition/name
          :definition/description
          :definition/correctResponsesPattern
          :definition/type
          :definition/moreInfo
          :definition/choices
          :definition/extensions])
   (restrict-keys :definition/name
                  :definition/description
                  :definition/correctResponsesPattern
                  :definition/interactionType
                  :definition/type
                  :definition/moreInfo
                  :definition/choices
                  :definition/extensions)))

(defmethod interaction-type "sequencing" [_]
  (s/and
   (s/keys
    :req [:definition/interactionType]
    :opt [:definition/name
          :definition/description
          :definition/correctResponsesPattern
          :definition/type
          :definition/moreInfo
          :definition/choices
          :definition/extensions])
   (restrict-keys :definition/name
                  :definition/description
                  :definition/correctResponsesPattern
                  :definition/interactionType
                  :definition/type
                  :definition/moreInfo
                  :definition/choices
                  :definition/extensions)))

(defmethod interaction-type "likert" [_]
  (s/and
   (s/keys
    :req [:definition/interactionType]
    :opt [:definition/name
          :definition/description
          :definition/correctResponsesPattern
          :definition/type
          :definition/moreInfo
          :definition/scale
          :definition/extensions])
   (restrict-keys :definition/name
                  :definition/description
                  :definition/correctResponsesPattern
                  :definition/interactionType
                  :definition/type
                  :definition/moreInfo
                  :definition/scale
                  :definition/extensions)))

(defmethod interaction-type "matching" [_]
  (s/and
   (s/keys
    :req [:definition/interactionType]
    :opt [:definition/name
          :definition/description
          :definition/correctResponsesPattern
          :definition/type
          :definition/moreInfo
          :definition/source
          :definition/target
          :definition/extensions])
   (restrict-keys :definition/name
                  :definition/description
                  :definition/correctResponsesPattern
                  :definition/interactionType
                  :definition/type
                  :definition/moreInfo
                  :definition/source
                  :definition/target
                  :definition/extensions)))

(defmethod interaction-type "performance" [_]
  (s/and
   (s/keys
    :req [:definition/interactionType]
    :opt [:definition/name
          :definition/description
          :definition/correctResponsesPattern
          :definition/type
          :definition/moreInfo
          :definition/steps
          :definition/extensions])
   (restrict-keys :definition/name
                  :definition/description
                  :definition/correctResponsesPattern
                  :definition/interactionType
                  :definition/type
                  :definition/moreInfo
                  :definition/steps
                  :definition/extensions)))

(defmethod interaction-type nil [_]
  (s/and
   (s/keys
    :opt [:definition/name
          :definition/description
          :definition/type
          :definition/moreInfo
          :definition/extensions])
   (restrict-keys :definition/name
                  :definition/description
                  :definition/type
                  :definition/moreInfo
                  :definition/extensions)))

(defmethod interaction-type :default [_]
  (s/and
   (s/keys
    :req [:definition/interactionType]
    :opt [:definition/name
          :definition/description
          :definition/correctResponsesPattern
          :definition/type
          :definition/moreInfo
          :definition/extensions])
   (restrict-keys :definition/name
                  :definition/description
                  :definition/correctResponsesPattern
                  :definition/interactionType
                  :definition/type
                  :definition/moreInfo
                  :definition/extensions)))


(s/def :activity/definition
  (conform-ns "definition"
              (s/multi-spec interaction-type (fn [gen-val _]
                                               gen-val
                                               #_:definition/interactionType))))

(s/def :activity/objectType
  #{"Activity"})

(s/def :activity/id
  ::iri)

(s/def ::activity
  (conform-ns "activity"
              (s/and
               (s/keys :req [:activity/id]
                       :opt [:activity/objectType
                             :activity/definition])
               (restrict-keys :activity/id
                              :activity/objectType
                              :activity/definition))))

;; Account

(s/def :account/name
  ::string-not-empty)

(s/def :account/homePage
  ::irl)

(s/def ::account
  (conform-ns "account"
              (s/and
               (s/keys :req [:account/name
                             :account/homePage])
               (restrict-keys :account/name
                              :account/homePage))))

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

(defn max-one-ifi
  "Assert that agents/groups only have one IFI"
  [a]
  (>= 1 (count (select-keys a [:agent/mbox
                               :agent/mbox_sha1sum
                               :agent/openid
                               :agent/account
                               :group/mbox
                               :group/mbox_sha1sum
                               :group/openid
                               :group/account]))))

(s/def ::agent
  (s/with-gen (s/and
               (s/conformer
                (partial conform-ns-map "agent")
                unform-ns-map)
               (s/keys :req [(or :agent/mbox
                                 :agent/mbox_sha1sum
                                 :agent/openid
                                 :agent/account)]
                       :opt [:agent/name
                             :agent/objectType])
               (restrict-keys :agent/mbox
                              :agent/mbox_sha1sum
                              :agent/openid
                              :agent/account
                              :agent/name
                              :agent/objectType)
               max-one-ifi)
    #(sgen/fmap
      unform-ns-map
      (s/gen (s/or :ifi-mbox
                   (s/keys :req [:agent/mbox]
                           :opt [:agent/name
                                 :agent/objectType])
                   :ifi-mbox_sha1sum
                   (s/keys :req [:agent/mbox_sha1sum]
                           :opt [:agent/name
                                 :agent/objectType])
                   :ifi-openid
                   (s/keys :req [:agent/openid]
                           :opt [:agent/name
                                 :agent/objectType])
                   :ifi-account
                   (s/keys :req [:agent/account]
                           :opt [:agent/name
                                 :agent/objectType]))))))

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
  (s/coll-of ::agent :kind vector? :into [] :gen-max 3))

(s/def ::identified-group
  (s/keys :req [:group/objectType
                (or :group/mbox
                    :group/mbox_sha1sum
                    :group/openid
                    :group/account)]
          :opt [:group/name
                :group/member]))

(s/def ::anonymous-group
  (s/and
   (s/keys :req [:group/objectType
                 :group/member]
           :opt [:group/name])
   #(-> % :group/member seq)))

(def identified-group?
  (comp
   some?
   (some-fn :group/mbox
            :group/mbox_sha1sum
            :group/openid
            :group/account)))

(defmulti group-type #(if (identified-group? %)
                        :group/identified
                        :group/anonymous))

(defmethod group-type :group/identified [_]
  ::identified-group)

(defmethod group-type :group/anonymous [_]
  ::anonymous-group)


(s/def ::group
  (s/with-gen (s/and
               (s/conformer
                (partial conform-ns-map "group")
                unform-ns-map)
               (s/multi-spec group-type (fn [gen-val _]
                                          gen-val))
               (restrict-keys :group/mbox
                              :group/mbox_sha1sum
                              :group/openid
                              :group/account
                              :group/name
                              :group/objectType
                              :group/member)
               max-one-ifi)
    #(sgen/fmap
      unform-ns-map
      (s/gen (s/or :ifi-mbox
                   (s/keys :req [:group/mbox]
                           :opt [:group/member
                                 :group/name
                                 :group/objectType])
                   :ifi-mbox_sha1sum
                   (s/keys :req [:group/mbox_sha1sum]
                           :opt [:group/member
                                 :group/name
                                 :group/objectType])
                   :ifi-openid
                   (s/keys :req [:group/openid]
                           :opt [:group/member
                                 :group/name
                                 :group/objectType])
                   :ifi-account
                   (s/keys :req [:group/account]
                           :opt [:group/member
                                 :group/name
                                 :group/objectType])
                   :anon
                   (s/keys :req [:group/member]
                           :opt [:group/name
                                 :group/objectType]))))))

;; Actor

(defmulti actor-type (fn [a]
                       (case (get a "objectType")
                         "Agent" :actor/agent
                         "Group" :actor/group
                         :actor/agent)))

(defmethod actor-type :actor/agent [_]
  ::agent)

(defmethod actor-type :actor/group [_]
  ::group)


(s/def ::actor
  (s/multi-spec actor-type (fn [gen-val _]
                             gen-val)))

;; Verb

(s/def :verb/id ::iri)

(s/def :verb/display ::language-map)

(s/def
  ::verb
  (conform-ns "verb"
              (s/and
               (s/keys :req [:verb/id]
                       :opt [:verb/display])
               (restrict-keys :verb/id
                              :verb/display))))

;; Result

(s/def :score/scaled
  (s/with-gen
    (s/and
     double-conformer
     (s/double-in :min -1.0 :max 1.0 :infinite? false :NaN? false))
    #(sgen/double* {:min -1.0 :max 1.0
                    :infinite? false
                    :NaN? false})))

(def safe-double-spec
  (s/with-gen
    (s/and
     double-conformer
     (s/double-in :infinite? false :NaN? false))
    #(sgen/double* {:infinite? false
                    :NaN? false})))

(s/def :score/raw
  safe-double-spec)

(s/def :score/min
  safe-double-spec)

(s/def :score/max
  safe-double-spec)

(defn valid-min-max-raw?
  [{raw :score/raw
    min :score/min
    max :score/max}]
  (if (or min raw max)
    (apply <= (filter identity [min raw max]))
    true))

(s/def :result/score
  (s/with-gen (s/and
                (s/conformer
                 (partial conform-ns-map "score")
                 unform-ns-map)
                (s/keys :opt [:score/scaled
                              :score/raw
                              :score/min
                              :score/max])
                (restrict-keys :score/scaled
                               :score/raw
                               :score/min
                               :score/max)
                valid-min-max-raw?)
     #(sgen/fmap
       unform-ns-map
       (sgen/such-that
        valid-min-max-raw?
        (sgen/not-empty
         (s/gen (s/keys :opt [:score/scaled
                              :score/raw
                              :score/min
                              :score/max])))))))

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

(s/def ::result
  (conform-ns "result"
              (s/and
               (s/keys :opt [:result/score
                             :result/success
                             :result/completion
                             :result/response
                             :result/duration
                             :result/extensions])
               (restrict-keys :result/score
                              :result/success
                              :result/completion
                              :result/response
                              :result/duration
                              :result/extensions))))

;; Statement Ref

(s/def :statement-ref/id ::uuid)

(s/def :statement-ref/objectType
  #{"StatementRef"})

(s/def ::statement-ref
  (conform-ns "statement-ref"
              (s/and
               (s/keys :req [:statement-ref/id
                             :statement-ref/objectType])
               (restrict-keys :statement-ref/id
                              :statement-ref/objectType))))

;; Context

(s/def ::context-activities-array
  (s/coll-of ::activity :kind vector? :into [] :min-count 1))

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

(s/def :context/contextActivities
  (conform-ns "contextActivities"
              (s/and
               (s/keys :req [(or :contextActivities/parent
                                 :contextActivities/grouping
                                 :contextActivities/category
                                 :contextActivities/other)])
               (restrict-keys :contextActivities/parent
                              :contextActivities/grouping
                              :contextActivities/category
                              :contextActivities/other))))

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

(s/def ::context
  (conform-ns "context"
              (s/and
               (s/keys :opt [:context/registration
                             :context/instructor
                             :context/team
                             :context/contextActivities
                             :context/revision
                             :context/platform
                             :context/language
                             :context/statement
                             :context/extensions])
               (restrict-keys :context/registration
                              :context/instructor
                              :context/team
                              :context/contextActivities
                              :context/revision
                              :context/platform
                              :context/language
                              :context/statement
                              :context/extensions))))

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
  (conform-ns "attachment"
              (s/keys :req [:attachment/usageType
                            :attachment/display
                            :attachment/contentType
                            :attachment/length
                            :attachment/sha2]
                      :opt [:attachment/description
                            :attachment/fileUrl])))

(s/def ::url-attachment
  (conform-ns "attachment"
              (s/keys :req [:attachment/usageType
                            :attachment/display
                            :attachment/contentType
                            :attachment/length
                            :attachment/sha2
                            :attachment/fileUrl]
                      :opt [:attachment/description])))

(s/def ::attachment
  (conform-ns "attachment"
              (s/and
               (s/keys :req [:attachment/usageType
                             :attachment/display
                             :attachment/contentType
                             :attachment/length
                             :attachment/sha2]
                       :opt [:attachment/description
                             :attachment/fileUrl])
               (restrict-keys
                :attachment/usageType
                :attachment/display
                :attachment/contentType
                :attachment/length
                :attachment/sha2
                :attachment/description
                :attachment/fileUrl))))

(s/def ::attachments
  (s/coll-of ::attachment :kind vector? :into [] :min-count 1))

;; Sub-statement

(s/def :sub-statement/actor
  ::actor)

(s/def :sub-statement/verb
  ::verb)

(defmulti sub-statement-object-type (fn [ss-o]
                                      (case (get ss-o "objectType")
                                        "Activity" :sub-statement-object/activity
                                        nil :sub-statement-object/activity
                                        "Agent" :sub-statement-object/agent
                                        "Group" :sub-statement-object/group
                                        "StatementRef" :sub-statement-object/statement-ref
                                        ::s/invalid)))

(defmethod sub-statement-object-type :sub-statement-object/activity [_]
  ::activity)

(defmethod sub-statement-object-type :sub-statement-object/agent [_]
  ::agent)

(defmethod sub-statement-object-type :sub-statement-object/group [_]
  ::group)

(defmethod sub-statement-object-type :sub-statement-object/statement-ref [_]
  ::statement-ref)

(s/def :sub-statement/object
  (s/multi-spec sub-statement-object-type (fn [gen-val _]
                                            gen-val)))

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

(s/def ::sub-statement
  (conform-ns "sub-statement"
              (s/and (s/keys :req [:sub-statement/actor
                                   :sub-statement/verb
                                   :sub-statement/object
                                   :sub-statement/objectType]
                             :opt [:sub-statement/result
                                   :sub-statement/context
                                   :sub-statement/attachments
                                   :sub-statement/timestamp])
                     (restrict-keys
                      :sub-statement/actor
                      :sub-statement/verb
                      :sub-statement/object
                      :sub-statement/objectType
                      :sub-statement/result
                      :sub-statement/context
                      :sub-statement/attachments
                      :sub-statement/timestamp)
                     (fn valid-context? [s]
                       (if (let [s-o (:sub-statement/object s)]
                             (or (:activity/objectType s-o)
                                 (:activity/id s-o)))
                         true
                         (not (some-> s :sub-statement/context revision-or-platform?)))))))

;; Authority

(s/def ::oauth-consumer
  (conform-ns "agent"
              (s/and
               (s/keys :req [:agent/account]
                       :opt [:agent/objectType
                             :agent/name])
               (restrict-keys :agent/account
                              :agent/objectType
                              :agent/name))))

(s/def :tlo-group/member
  (s/with-gen
    (s/cat
     :oauth-consumer
     ::oauth-consumer
     :agent
     ::agent)
    #(sgen/fmap vec (s/gen (s/cat
                            :oauth-consumer
                            ::oauth-consumer
                            :agent
                            ::agent)))))

(s/def :tlo-group/objectType #{"Group"})
(s/def :tlo-group/name ::string-not-empty)
(s/def :tlo-group/mbox :group/mbox)
(s/def :tlo-group/mbox_sha1sum :group/mbox_sha1sum)
(s/def :tlo-group/openid :group/openid)
(s/def :tlo-group/account :group/account)

(s/def ::tlo-group
  (conform-ns "tlo-group"
              (s/and (s/keys :req [:tlo-group/objectType
                                   :tlo-group/member]
                             :opt [
                                   :tlo-group/name])
                     (restrict-keys :tlo-group/objectType
                                    :tlo-group/member
                                    :tlo-group/name))))


;; Statement!

(s/def :statement/authority
  (s/or :agent
        ::agent
        :oauth-consumer
        ::oauth-consumer
        :three-legged-oauth-group
        ::tlo-group))

(defmulti statement-object-type (fn [ss-o]
                                  (case (get ss-o "objectType")
                                    "Activity" :statement-object/activity
                                    nil :statement-object/activity
                                    "Agent" :statement-object/agent
                                    "Group" :statement-object/group
                                    "StatementRef" :statement-object/statement-ref
                                    "SubStatement" :statement-object/sub-statement
                                    ::s/invalid)))

(defmethod statement-object-type :statement-object/activity [_]
  ::activity)

(defmethod statement-object-type :statement-object/agent [_]
  ::agent)

(defmethod statement-object-type :statement-object/group [_]
  ::group)

(defmethod statement-object-type :statement-object/statement-ref [_]
  ::statement-ref)

(defmethod statement-object-type :statement-object/sub-statement [_]
  ::sub-statement)


(s/def :statement/object
  (s/multi-spec statement-object-type (fn [gen-val _]
                                        gen-val)))

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

(s/def ::statement
  (conform-ns "statement"
              (s/and
               (s/keys :req [:statement/actor
                             :statement/verb
                             :statement/object]
                       :opt [:statement/id
                             :statement/result
                             :statement/context
                             :statement/timestamp
                             :statement/stored
                             :statement/authority
                             :statement/attachments
                             :statement/version])
               (restrict-keys
                :statement/actor
                :statement/verb
                :statement/object
                :statement/id
                :statement/result
                :statement/context
                :statement/timestamp
                :statement/stored
                :statement/authority
                :statement/attachments
                :statement/version)
               (fn valid-context? [s]
                 (if (let [s-o (:statement/object s)]
                       (or (:activity/objectType s-o)
                           (:activity/id s-o)))
                   true
                   (not (some-> s :statement/context revision-or-platform?))))
               (fn valid-void? [s]
                 (if (some-> s :statement/verb :verb/id (= "http://adlnet.gov/expapi/verbs/voided"))
                   (some-> s :statement/object :statement-ref/objectType)
                   true)))))

(s/def ::statements
  (s/coll-of ::statement :into [] :min-count 1))
