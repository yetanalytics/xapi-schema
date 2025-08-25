(ns xapi-schema.spec-2-0-0.regex)

(defn- base-timestamp []
  (let [;; Date
        year  "(\\d{4})"
        month "(0[1-9]|1[0-2])"
        day   "(0[1-9]|[12]\\d|3[01])" ; ignore month/leap year constraints
        ;; Time
        hour "([01]\\d|2[0-3])"
        min  "([0-5]\\d)"
        sec  "([0-5]\\d|60)" ; leap seconds
        sec-frac "(\\.\\d+)"
        ;; Time
        time (str "(?:" hour ":" min ":" sec sec-frac "?" ")")
        date (str "(?:" year "-" month "-" day ")")]
    (str date "[T\\s]" time)))

(def TimestampRegEx ; RFC 3339
  (let [;; Time
        hour "(?:[01]\\d|2[0-3])"
        min  "(?:[0-5]\\d)"
        ;; Offset
        lookahead   "(?!-00:00)"
        num-offset  (str "(?:[+-]" hour ":" min ")")
        time-offset (str "(Z|" lookahead num-offset ")")]
    (re-pattern (str "^" (base-timestamp) time-offset "$"))))

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
    (re-pattern (str "^P(?:" duration ")|P(?:" (base-timestamp) ")$"))))

;; Based on http://www.regexr.com/39s32
(def xAPIVersionRegEx
  (let [suf-part "[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*"
        suffix   (str "(\\.[0-9]+(?:-" suf-part ")?(?:\\+" suf-part ")?)?")
        ver-str  (str "^(1\\.0" suffix ")|(2\\.0\\.0)$")]
    (re-pattern ver-str)))
