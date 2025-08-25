(ns xapi-schema.spec-2-0-0.regex-test
  (:require
   [clojure.test :refer [deftest is testing] :include-macros true]
   [xapi-schema.spec-2-0-0.regex :refer [TimestampRegEx
                                         xAPIVersionRegEx
                                         DurationRegEx]]))
(deftest timestamp-regex-test
  (testing "matches valid ISO 8601 datetime stamps within the rfc3339 profile"
    (is (re-matches TimestampRegEx "2015-05-13T15:16:00Z"))
    (is (re-matches TimestampRegEx "2015-05-13T15:16:00.304Z"))
    (is (re-matches TimestampRegEx "2015-05-13T15:16:00-20:00"))
    (is (re-matches TimestampRegEx "2016-11-22T16:50:25.3868080Z"))
    (is (re-matches TimestampRegEx "0003-06-04T12:30:05Z")) ; Duration example
    (is (not (re-matches TimestampRegEx "5-13-2015")))
    (is (not (re-matches TimestampRegEx "20150513T15Z")))
    (is (not (re-matches TimestampRegEx "20150513T15:16:00Z")))
    ;; negative offset
    (is (not (re-matches TimestampRegEx "2008-09-15T15:53:00.601-00:00"))))
  (testing "matches valid but terrible stamps in rfc3339 OUTSIDE of 8601"
    (is (re-matches TimestampRegEx "2015-05-13 15:16:00Z"))))

(deftest xapi-version-regex-test
  (testing "matches xAPI 1.0.X versions"
    (is (and (re-matches xAPIVersionRegEx "1.0.0")
             (re-matches xAPIVersionRegEx "1.0.2")
             (re-matches xAPIVersionRegEx "1.0")
             (re-matches xAPIVersionRegEx "1.0.32-abc.def+ghi.jkl")))
    (is (not (re-matches xAPIVersionRegEx "0.9.5"))))
  (testing "matches xAPI 2.0.0 version only"
    (is (and (re-matches xAPIVersionRegEx "2.0.0")
             (not (re-matches xAPIVersionRegEx "2.0.2"))))))

(deftest duration-regex-test
  (testing "matches ISO durations"
    (is (re-matches DurationRegEx "P3Y6M4DT12H30M5S"))
    (is (re-matches DurationRegEx "P23DT122.34S"))
    (is (re-matches DurationRegEx "PT3H0M25.51S"))
    (is (re-matches DurationRegEx "PT3H25.51S"))
    (is (re-matches DurationRegEx "P0003-06-04T12:30:05")) ; Wikipedia example
    (is (not (re-matches DurationRegEx "PT")))
    (is (not (re-matches DurationRegEx "P10.3DT1.7S")))))
