(ns xapi-schema.support.data
  #?(:clj (:require [cheshire.core :as c]
                    [clojure.java.io :refer [reader]])
     :cljs (:require-macros [xapi-schema.support.data :refer [load-json
                                                              load-json-map]])))

;; Load test json files
#?(:clj
    (defmacro load-json
      "load a json file as edn (string keys)"
      [path]
      (c/parse-stream (reader path))))

#?(:clj
   (defmacro load-json-map
     "loads all names (being json files at base-path) into a
     map with the keyworded names for keys"
     [base-path names]
     (into {}
           (for [n names
                 :let [path (str base-path n ".json")]]
             [(keyword n) (c/parse-stream (reader path))]))))



(def language-tag
  "en-US")

(def language-map
  {language-tag "foo"})

(def iri
  "http://foo.com/bar")

(def mailto
  "mailto:milt@yetanalytics.com")

(def irl
  "www.foo.com")

(def extensions
  {"http://www.foo.bar" {"arbitrary" "data"}})

(def openid
  iri)

(def uuid
  "f47ac10b-58cc-4372-a567-0e02b2c3d479")

(def timestamp
  "2014-09-10T14:12:05Z")

(def duration
  "P3Y6M4DT12H30M5S")

(def version
  "1.0.0")

(def sha2
  "672fa5fa658017f1b72d65036f13379c6ab05d4ab3b6664908d8acf0b6a0c634")

(def sha1
  "123")

(def interaction-component
  {"id" "1"
   "description" {"en-US" "foo"}})

(def definition
  {"name"
   {"en-US" "simple statement"}
   "description"
   {"en-US"
    "A simple Experience API statement. Note that the LRS
                     does not need to have any prior information about the Actor (learner), the
                     verb, or the Activity/object."}})

(def activity
  {"id" iri
   "definition" definition})

(def account
  {"homePage" irl
   "name" "bob"})

(def agent
  {"mbox" mailto
   "objectType" "Agent"})

(def group
  {"mbox" mailto
   "objectType" "Group"})

(def anon-group
  {"objectType" "Group"
   "member" [agent]})

(def verb
  {"id" iri
   "display" language-map})

(def score
  {"max" 10
   "min" 1
   "raw" 5
   "scaled" 1.0})

(def result
  {"score" score
   "success" true
   "completion" true
   "response" "looking good!"
   "duration" duration
   "extensions" extensions})

(def statement-ref
  {"objectType" "StatementRef"
   "id" uuid})

(def context-activities
  {"parent" [activity]
   "grouping" [activity]
   "category" [activity]
   "other" [activity]})

(def context
  {"registration" uuid
   "instructor" agent
   "team" group
   "contextActivities" context-activities
   "revision" "whatever"
   "platform" "eeniac"
   "language" language-tag
   "statement" statement-ref
   "extensions" extensions})

(def attachment
  {"usageType" "http://foo.bar/baz"
   "display" language-map
   "contentType" "application/json"
   "length" 1024
   "sha2" sha2
   "fileUrl" "http://foo.bar/baz"})

(def sub-statement
  {"actor" agent
   "verb" verb
   "object" activity
   "objectType" "SubStatement"})

(def statement
  {"id" uuid
   "actor" agent
   "verb" verb
   "object" activity
   "timestamp" timestamp
   "stored" timestamp
   "authority" agent
   "version" version})

(def simple-statement
  (load-json "resources/data/statements/simple.json"))

(def long-statement
  (load-json "resources/data/statements/long.json"))

(def completion-statement
  (load-json "resources/data/statements/completion.json"))

(def void-statement
  (load-json "resources/data/statements/void.json"))

(def interaction-activity-defs
  (load-json-map
   "resources/data/objects/activity/definition/interaction/"
   ["choice" "fill-in" "likert" "long-fill-in"
    "matching" "numeric" "other" "performance"
    "sequencing" "true-false"]))

(def adl-sub-statement
  (load-json
   "resources/data/objects/sub-statement.json"))

(def authority-group
  (load-json
   "resources/data/objects/authority.json"))
