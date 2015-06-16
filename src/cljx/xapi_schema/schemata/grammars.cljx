(ns xapi-schema.schemata.grammars
  #+cljs (:require-macros [xapi-schema.schemata.grammars :refer [rfc-6068-mailto-abnf
                                                                 std-66-uri-abnf
                                                                 rfc-3987-iri-abnf
                                                                 rfc-5646-ltag-abnf
                                                                 rfc-3339-iso-date-time-abnf
                                                                 rfc-3339-duration-abnf
                                                                 rfc-4122-uuid-abnf]])
  (:require [instaparse.core :as insta]))


(defmacro rfc-6068-mailto-abnf []
  (slurp "resources/grammars/rfc_6068.abnf"))

(def rfc-6068-mailto
        (insta/parser
         (rfc-6068-mailto-abnf)
         :input-format :abnf))

(defn rfc-6068-mailto? [maybe-mailto]
        (not (insta/failure? (rfc-6068-mailto maybe-mailto))))




(defmacro std-66-uri-abnf []
  (slurp "resources/grammars/std_66.abnf"))


(def std-66-uri
  (insta/parser
   (std-66-uri-abnf)
   :input-format :abnf))

(defn std-66-uri? [maybe-uri]
  (not (insta/failure? (std-66-uri maybe-uri))))





(defmacro rfc-3987-iri-abnf []
  (slurp "resources/grammars/rfc_3987.abnf"))

#+clj (def rfc-3987-iri
  (insta/parser
   (rfc-3987-iri-abnf)
   :input-format :abnf))

#+clj (defn rfc-3987-iri? [maybe-iri]
  (not (insta/failure? (rfc-3987-iri maybe-iri))))






(defmacro rfc-5646-ltag-abnf []
  (slurp "resources/grammars/rfc_5646.abnf"))

#+clj (def rfc-5646-ltag
        (insta/parser
         (rfc-5646-ltag-abnf)
         :input-format :abnf))

#+clj (defn rfc-5646-ltag? [maybe-ltag]
        (not (insta/failure? (rfc-5646-ltag maybe-ltag))))



(defmacro rfc-3339-iso-date-time-abnf []
  (slurp "resources/grammars/rfc_3339_dt.abnf"))

(def rfc-3339-iso-date-time
        (insta/parser
         (rfc-3339-iso-date-time-abnf)
         :input-format :abnf))

(defn rfc-3339-date-time? [maybe-dt]
        (not (insta/failure? (rfc-3339-iso-date-time maybe-dt))))


(defmacro rfc-3339-duration-abnf []
  (slurp "resources/grammars/rfc_3339_du.abnf"))

(def rfc-3339-duration
  (insta/parser
   (rfc-3339-duration-abnf)
   :input-format :abnf))

(defn rfc-3339-duration? [maybe-du]
  (not (insta/failure? (rfc-3339-duration maybe-du))))



(defmacro rfc-4122-uuid-abnf []
  (slurp "resources/grammars/rfc_4122.abnf"))

(def rfc-4122-uuid
  (insta/parser
   (rfc-4122-uuid-abnf)
   :input-format :abnf))

(defn rfc-4122-uuid? [maybe-uuid]
  (not (insta/failure? (rfc-4122-uuid maybe-uuid))))
