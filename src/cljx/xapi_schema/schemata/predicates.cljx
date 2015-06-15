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
  (s/both (s/pred valid-revision? :predicates/revision-not-allowed)
          (s/pred valid-platform? :predicates/platform-not-allowed)))

(defn regex-pred
  [regex message]
  (s/pred #(not (nil? (re-matches regex %)))
          message))

(def no-multi-ifi-pred
  (s/pred no-multi-ifi? :predicates/no-multi-ifi))

(def one-ifi-required-pred
  (s/both no-multi-ifi-pred
          (s/pred ifi-present? :predicates/no-ifi)))

(def void-statement-ref-pred
  (s/pred (fn [{:strs [verb object]}]
            (if (= (get verb "id") "http://adlnet.gov/expapi/verbs/voided")
              (= (get object "objectType") "StatementRef")
              true))
          :predicates/void-statement-ref))


;; validation predicate schemata

(def AgentValidations
  one-ifi-required-pred)

(def GroupValidations
  (s/conditional ifi-present? no-multi-ifi-pred ;; identified group, only one IFI
                 :else (s/pred (fn [{:strs [member]}]
                                 (not (nil? member))) :predicates/no-anon-group-member)))

(def InteractionComponentsValidations
  (s/pred unique-ids? :predicates/distinct-ic-ids))

(def DefinitionValidations
  (s/pred valid-component-keys? :predicates/valid-component-keys))


(def ScoreValidations
  (s/both (s/pred (fn
                    [x]
                    (let [{:strs [raw max]} x]
                      (if (and raw max)
                        (<= raw max)
                        true))) :predicates/score-lt-max)
          (s/pred (fn
                    [x]
                      (let [{:strs [raw min]} x]
                        (if (and raw min)
                          (>= raw min)
                          true))) :predicates/score-gt-min)
          (s/pred (fn
                    [x]
                    (let [{:strs [min max]} x]
                      (if (and min max)
                        (< min max)
                        true))) :predicates/score-lt-max)))

(def StatementValidations
  (s/both
   valid-context-pred
   void-statement-ref-pred))
