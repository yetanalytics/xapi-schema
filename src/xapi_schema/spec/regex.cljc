(ns xapi-schema.spec.regex)

(def LanguageTagRegEx ; RFC 5646
  (let [;; Language (note - tag semantics are ignored)
        l1   "(?:[A-Za-z]{2,3}(?:-(?:[A-Za-z]{3}(?:-[A-Za-z]{3}){0,2}))?)"
        l2   "[A-Za-z]{4}"
        l3   "[A-Za-z]{5,8}"
        lang (str "(?:" l1 "|" l2 "|" l3 ")")
        ;; Suffixes
        script  "(?:-[A-Za-z]{4})?"
        region  "(?:-(?:[A-Za-z]{2}|[0-9]{3}))?"
        variant "(?:-(?:[A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*"
        ext     "(?:-(?:[0-9A-WY-Za-wy-z](?:-[A-Za-z0-9]{2,8})+))*"
        private "(?:-(?:x(?:-[A-Za-z0-9]{1,8})+))?"
        ;; Grandfathered tags
        grand-irreg "(?:en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)"
        grand-reg   "(?:art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)"
        ;; Tag
        tag (str "^(?:"
                 "(?:" lang script region variant ext private ")"
                 "|" grand-irreg
                 "|" grand-reg
                 ")$")]
    (re-pattern tag)))

(def OpenIdRegEx
  #?(:clj #"^((((http|https):(?:\/{1,2})?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-\[\]\:\+]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w\-]*))?)$"
     :cljs #"^((((http|https):(?:/{1,2})?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-\[\]\:\+]+)((?:/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w\-]*))?)$"))

(def AbsoluteIRIRegEx
  #?(:clj #"^((([A-Za-z\+\-\.]+:(?:\/{1,2})?)(?:[\-;\*\(\)!\':&=\+\$,%\w]+@)?[A-Za-z0-9\.\-\_\[\]\:\+%]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\*\.\w_]*)#?(?:[\:\$\&\'\(\)+,;\.\!%\*\/\\\w\-\?=]*))?)$"
     :cljs #"^((([A-Za-z\+\-\.]+:(?:/{1,2})?)(?:[\-;\*\(\)!\':&=\+\$,%\w]+@)?[A-Za-z0-9\.\-\_\[\]\:\+%]+)((?:/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\*\.\w_]*)#?(?:[\:\$\&\'\(\)+,;\.\!%\*\/\\\w\-\?=]*))?)$"))

(def RelativeIRLRegEx
  #?(:clj  #"^(((?:\/[\+~%\/\.\w\-_]*)\??(?:[\-\+=&;%@\*\.\w_]*)#?(?:[\:\$\&\'\(\)+,;\.\!\*\/\\\w\-\?=]*))?)$"
     :cljs #"^(((?:/[\+~%\/\.\w\-_]*)\??(?:[\-\+=&;%@\*\.\w_]*)#?(?:[\:\$\&\'\(\)+,;\.\!\*\/\\\w\-\?=]*))?)$"))

(def MailToIRIRegEx
  (let [username "(?:[a-zA-Z0-9!#$&'*+/=/.?^_`{|}~-]|%[0-9a-fA-F]{2})+"
        domain   "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9]+"]
    (re-pattern (str "mailto:" username "@" domain))))

(def UuidRegEx ; RFC 3984
  (re-pattern (str "[0-9A-Fa-f]{8}-" ; [0-9A-Fa-f] = hex digit
                   "[0-9A-Fa-f]{4}-"
                   "[1-4][0-9A-Fa-f]{3}-"
                   "[0-9A-Fa-f]{4}-"
                   "[0-9A-Fa-f]{12}")))

(def base-timestamp
  (let [;; Date
        year  "(?:\\d{4})"
        month "(?:0[1-9]|1[0-2])"
        day   "(?:0[1-9]|[12]\\d|3[01])" ; ignore month/leap year constraints
        ;; Time
        hour "(?:[01][0-9]|2[0-3])"
        min  "(?:[0-5]\\d)"
        sec  "(?:[0-5]\\d|60)" ; leap seconds
        sec-frac "(?:\\.\\d+)"
        ;; Time
        time (str "(?:" hour ":" min ":" sec sec-frac "?" ")")
        date (str "(?:" year "-" month "-" day ")")]
    (str date "T" time)))

(def TimestampRegEx ; RFC 3339
  (let [;; Time
        hour "(?:[01][0-9]|2[0-3])"
        min  "(?:[0-5]\\d)"
        ;; Offset
        lookahead   "(?!-00:00)"
        num-offset  (str "(?:[+-]" hour ":" min ")")
        time-offset (str "(?:Z|" lookahead num-offset ")")]
    (re-pattern (str "^" base-timestamp time-offset "$"))))

;; Based on http://www.regexr.com/39s32
(def xAPIVersionRegEx
  (let [suf-part "[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*"
        suffix   (str "(\\.[0-9]+(?:-" suf-part ")?(?:\\+" suf-part ")?)?")
        ver-str  (str "^1\\.0" suffix "$")]
    (re-pattern ver-str)))

(def DurationRegEx ; ISO 8601 Durations
  (let [dy "(?:\\d+Y|\\d+\\.\\d+Y$)"
        dm "(?:\\d+M|\\d+\\.\\d+M$)"
        dw "(?:\\d+W|\\d+\\.\\d+W$)"
        dd "(?:\\d+D|\\d+\\.\\d+D$)"
        dh "(?:\\d+H|\\d+\\.\\d+H$)"
        ds "(?:\\d+S|\\d+\\.\\d+S$)"
        dur-date (str "(?:" dd "|" dm dd "?" "|" dy dm "?" dd "?" ")")
        dur-time (str "(?:" ds "|" dm ds "?" "|" dh dm "?" ds "?" ")")
        dur-week (str "(?:" dw ")")
        duration (str "(?:" dur-date "(?:T" dur-time ")?" ")" "|"
                      "(?:T" dur-time ")" "|"
                      dur-week)]
    (re-pattern (str "^P(?:" duration ")|P(?:" base-timestamp ")$"))))

(def Base64RegEx
  (let [body   "(?:[A-Za-z0-9\\+\\/]{4})*"
        suffix (str "(?:"
                    "[A-Za-z0-9\\+\\/]{2}==|"
                    "[A-Za-z0-9\\+\\/]{3}=|"
                    "[A-Za-z0-9\\+\\/]{4}"
                    ")")]
    (re-pattern (str "^" body suffix "$"))))

(def Sha1RegEx
  #"^[0-9a-fA-F]{40}$")
