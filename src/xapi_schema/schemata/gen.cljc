(ns xapi-schema.schemata.gen
  (:require
   [xapi-schema.schemata.spec :as xapi]
   [clojure.spec :as s]
   [clojure.test.check.generators :as gen]
   [clojure.spec.gen :as sgen]
   [com.gfredericks.test.chuck.generators :as cgen]
   [clojure.walk :as w]))

(def ltag-gen
  (gen/resize 1 (cgen/string-from-regex
   ;; #"[a-z]{2}(-[A-Z]{2})?"
   #"(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)|((en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)))"
   )))

(def openid-gen
  (cgen/string-from-regex
   #"((((http|https):(?:\/{1,2})?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-\[\]\:\+]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w\-]*))?)"))

(def iri-gen
  (cgen/string-from-regex
   #"((([A-Za-z\+\-\.]+:(?:\/{1,2})?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-\[\]\:\+]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w\-\?=]*))?)"))

(def mailto-gen
  (gen/resize 3 (cgen/string-from-regex
   #"mailto:(?:[a-zA-Z0-9!#$&'*+/=?^_`{|}~-]|%[0-9a-fA-F]{2})+(?:\.(?:[a-zA-Z0-9!#$&'*+/=?^_`{|}~-]|%[0-9a-fA-F]{2})+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?")))

(def uuid-gen
  (gen/fmap
   str
   (sgen/uuid)))

(def timestamp-gen
  (cgen/string-from-regex
   #"(\d{4})((-)((0[1-9]|1[0-2])((-)([12]\d|0[1-9]|3[01]))))((T(0[0-9]|1[0-9]|2[0-3])(:([0-4][0-9]|5[0-9])){2}(\.\d+)?)|T23:59:60)(Z|([+-](0[0-9]|1[0-9]|2[0-3])(:([0-4][0-9]|5[0-9]))))"))

(def version-gen
  (cgen/string-from-regex
   #"((1\.0\.([0-9]+)(?:-([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?)(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?)"))


(def duration-gen
  (cgen/string-from-regex
   #"P(\d+((\.|,)\d*)?Y)?(\d+((\.|,)\d*)?M)?(\d+((\.|,)\d*)?W)?(\d+((\.|,)\d*)?D)?(T(\d+((\.|,)\d*)?H)?(\d+((\.|,)\d*)?M)?(\d+((\.|,)\d*)?S)?)?"))

(def base64-gen
  (cgen/string-from-regex
   #"(?:[A-Za-z0-9\+\/]{4})*(?:[A-Za-z0-9\+\/]{2}==|[A-Za-z0-9\+\/]{3}=|[A-Za-z0-9\+\/]{4})"))

(def sha1-gen
  (cgen/string-from-regex
   #"[0-9a-fA-F]{40}"))

(def overrides
  {::xapi/language-tag (constantly ltag-gen)
   ::xapi/iri          (constantly iri-gen)
   ::xapi/mailto-iri   (constantly mailto-gen)
   ::xapi/irl          (constantly iri-gen)
   ::xapi/openid       (constantly openid-gen)
   ::xapi/uuid         (constantly uuid-gen)
   ::xapi/timestamp    (constantly timestamp-gen)
   ::xapi/duration     (constantly duration-gen)
   ::xapi/version      (constantly version-gen)
   ::xapi/sha2         (constantly base64-gen)
   ::xapi/sha1sum      (constantly sha1-gen)})

;; redefs
(s/def ::xapi/interaction-component
  ::xapi/interaction-component*)

(s/def :activity/definition
  :activity/definition*)

(s/def ::xapi/activity
  ::xapi/activity*)

(s/def ::xapi/account
  ::xapi/account*)

(s/def ::xapi/agent
  ::xapi/agent*)

(s/def ::xapi/group
  ::xapi/group*)

(s/def ::xapi/verb
  ::xapi/verb*)

(s/def :result/score
  :result/score*)

(s/def ::xapi/result
  ::xapi/result*)

(s/def ::xapi/statement-ref
  ::xapi/statement-ref*)

(s/def :context/contextActivities
  :context/contextActivities*)

(s/def ::xapi/context
  ::xapi/context*)

(s/def ::xapi/attachment
  ::xapi/attachment*)

(s/def ::xapi/sub-statement
  ::xapi/sub-statement*)

(s/def ::xapi/three-legged-oauth-group
  ::xapi/three-legged-oauth-group*)

(s/def ::xapi/statement
  ::xapi/statement*)

(def statement-gen
  (sgen/fmap
   w/stringify-keys
   (s/gen ::xapi/statement overrides)))

(def statements-gen
  (sgen/fmap
   w/stringify-keys
   (s/gen ::xapi/statements overrides)))
