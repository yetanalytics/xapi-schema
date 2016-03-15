(ns xapi-schema.schemata.i18n
  #?(:clj (:require [taoensso.tower :as tower :refer [make-t]])
     :cljs (:require [taoensso.tower :as tower
                      :refer [make-t]
                      :refer-macros (with-tscope dict-compile*)])))

(def tower-config
  {:fallback-locale :en
   #?@(:clj [:dictionary "i18n/dict.clj"]
       :cljs [:compiled-dictionary (dict-compile* "i18n/dict.clj")])
                                        ;:dev-mode? true ; Set to true for auto dictionary reloading
   })

(def t* (make-t tower-config))

(defn t
  "Return a localized string for key, or return the stringed key if not found"
  [ltag k]
  (let [localized (t* ltag k)]
    (if-not (= localized "")
      localized
      (str k))))
