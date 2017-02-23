(ns xapi-schema.schemata.predicates
  (:require
   [clojure.set :refer [intersection
                        difference]]))

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

;; anon group

(defn has-members?
  [{:strs [member]}]
  (seq member))

;; three-legged-oauth group

(defn two-members? [m]
  (= 2 (count m)))

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
         submitted-keys   (intersection (set (keys data)) component-keys)
         valid-for-type   (valid-component-keys interaction-type)
         invalid          (difference submitted-keys valid-for-type)]

     (if (and interaction-type (seq invalid))
       false

       ;; If any of these component keys are given, an interactionType must be present
       (if (some (conj component-keys "correctResponsesPattern")
                 (keys data))
         (if interaction-type
           true
           false)
         true)))
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

;; Score pred fns

(defn valid-score?
  [x]
  (let [{:strs [min max raw scaled]} x]
    (and
     (if scaled
       (<= -1.0 scaled 1.0)
       true)
     (if (or min raw max)
       (apply <= (filter identity [min raw max]))
       true))))

;; statement predicate fns
(defn valid-void? [{:strs [verb object]}]
  (if (= (get verb "id") "http://adlnet.gov/expapi/verbs/voided")
    (= (get object "objectType") "StatementRef")
    true))

;; util
(defn re-pred
  [re]
  #(not
    (nil? (re-matches re %))))
