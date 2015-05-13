(ns xapi-schema.schemata.predicates
  (:require
   [clojure.set :refer [intersection
                        difference]]
   [xapi-schema.schemata.regex :refer [AbsoluteIRIRegEx
                                       UuidRegEx]]
   #+clj [schema.core :as s]
   #+cljs [schema.core :as s
           :include-macros true]))

;; IFI predicates
(defn ifi-count
  "returns the number of IFI keys in map x"
  [x]
  (count
   (intersection
    (set (keys x))
    #{"mbox" "mbox_sha1sum" "openid" "account"})))

(defn no-multi-ifi?
  "returns true unless there is more than one IFI key in map x"
  [x]
  (not (> (ifi-count x)
          1)))

(defn ifi-present?
  "returns true unless there are no IFI keys in map x"
  [x]
  (not (= (ifi-count x)
          0)))

(defn no-ifi?
  "returns true if there is no IFI key in map x"
  [x]
  (= (ifi-count x)
     0))

;; interaction component predicates

(defn unique-ids?
  [data]
  "Validates that the ID string values in a list of maps are unique."
  (if (or (vector? data)
          (set? data)) ;; if not a list, we let something else catch it
   (let [ids (map #(% "id") data)]
    (apply distinct? ids))
    true))

(def component-keys
  #{"choices" "scale" "target" "steps" "source"})

(def valid-component-keys
  "Given an interactionType, what component keys are valid?"
  {"choice"      #{"choices"}
   "sequencing"  #{"choices"}
   "likert"      #{"scale"}
   "matching"    #{"source" "target"}
   "performance" #{"steps"}
   "true-false"  #{}
   "fill-in"     #{}
   "numeric"     #{}
   "other"       #{}})


(defn valid-component-keys?
  [data]
  "Predicate to ensure valid component list keys"
  (if (map? data)
   (let [interaction-type (data "interactionType")
        submitted-keys (intersection (set (keys data)) component-keys)
        valid-for-type (valid-component-keys interaction-type)
        invalid (difference submitted-keys valid-for-type)]

    (if (and interaction-type (seq invalid))
      false
      true))
    true))

;; context predicates

(defn valid-revision?
  [data]
  (let [activity-type (get-in data ["object" "objectType"])
        activity-object? (or (nil? activity-type)
                             (= "Activity" activity-type))
        revision-present? (get-in data ["context" "revision"])]
    (if activity-object?
      true
      (not revision-present?))))

(defn valid-platform?
  [data]
  (let [activity-type (get-in data ["object" "objectType"])
        activity-object? (or (nil? activity-type)
                             (= "Activity" activity-type))
        platform-present? (get-in data ["context" "platform"])]
    (if activity-object?
      true
      (not platform-present?))))

;; Predicate schemata, builders

(def valid-context-pred
  (s/both (s/pred valid-revision? "Context revision only valid if Statement object is an Activity.")
          (s/pred valid-platform? "Context platform only valid if Statement object is an Activity.")))

(defn regex-pred
  [regex message]
  (s/pred #(not (nil? (re-matches regex %)))
          message))

(def no-multi-ifi-pred
  (s/pred no-multi-ifi? "valid number: of IFI's (one)"))

(def one-ifi-required-pred
  (s/both no-multi-ifi-pred
          (s/pred ifi-present? "ifi present")))

;; validation predicate schemata

(def AgentValidations
  one-ifi-required-pred)

(def GroupValidations
  (s/conditional ifi-present? no-multi-ifi-pred ;; identified group, only one IFI
                 :else (s/pred (fn [{:strs [member]}]
                                 (not (nil? member))) "present: member key on anonymous group")))

(def ActivityValidations
  (s/both (s/pred (fn [{:strs [id]}]
                    (not (nil? id))) "present: activity id")
          (s/pred (fn [{:strs [id]}]
                    (not (nil? (re-matches AbsoluteIRIRegEx id)))) "valid IRI: activity id")))

(def InteractionComponentsValidations
  (s/pred unique-ids? "distinct: interaction component IDs"))

(def DefinitionValidations
  (s/pred valid-component-keys? "valid Interaction Component List key(s)"))

(def StatementReferenceValidations
  (s/pred (fn [{:strs [id]}]
            (not (nil? (re-matches UuidRegEx id)))) "valid Uuid: Statement Reference id"))



  (def ScoreValidations
    (s/both (s/pred (fn
                      [x]
                      (let [{:strs [raw max]} x]
                        (if (and raw max)
                          (<= raw max)
                          true))) "raw cannot be higher than max")
            (s/pred (fn
                      [x]
                      (let [{:strs [raw min]} x]
                        (if (and raw min)
                          (>= raw min)
                          true))) "raw cannot be lower than min")
            (s/pred (fn
                      [x]
                      (let [{:strs [min max]} x]
                        (if (and min max)
                          (< min max)
                          true))) "min cannot be higer than max")))

(def StatementValidations
  valid-context-pred)

(def SubStatementValidations
  (s/both StatementValidations
          (s/pred (fn [{:strs [id stored version authority]}]
                    (nil? (or id stored version authority))) "SubStatements cannot have the id, stored, version or authority keys")))


(def OAuthConsumerValidations
  (s/pred (fn [x]
            (if (x "account")
              true
              false)) "valid IFI for oauth consumer: Account"))

(def AuthorityGroupValidations
  (s/pred (fn [x]
            (if (and (nil? (x "member")) (= (x "objectType") "Group"))
              false
              true)) "valid 3 legged OAuth Group: member required")
  )
