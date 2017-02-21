(ns xapi-schema.schemata.regex-test
  (:require
   #?@(:cljs [[cljs.test :refer-macros [deftest is testing run-tests]]
              [xapi-schema.schemata.regex :refer [LanguageTagRegEx
                                                  AbsoluteIRIRegEx
                                                  MailToIRIRegEx
                                                  UuidRegEx
                                                  TimestampRegEx
                                                  xAPIVersionRegEx
                                                  DurationRegEx
                                                  Base64RegEx
                                                  Sha1RegEx
                                                  OpenIdRegEx]]]
       :clj [[clojure.test :refer :all]
            [xapi-schema.schemata.regex :refer :all]])))

(deftest language-tag-regex-test
  (testing "matches valid Language Tags"
    (is (re-matches LanguageTagRegEx "en-US"))
    (is (not (re-matches LanguageTagRegEx "not a language tag")))))

(deftest open-id-regex-test
  (testing "matches valid URIs"
    (is (re-matches OpenIdRegEx "http://www.foo.com"))
    (is (not (re-matches OpenIdRegEx "www.foo.com")))
    (is (not (re-matches OpenIdRegEx "foo.com")))
    (is (not (re-matches OpenIdRegEx "hey dude wat"))))
  (testing "matches URIs with fragments"
    (is (re-matches OpenIdRegEx "http://example.com/xapi/verbs#sent-a-statement"))))

(deftest absolute-iri-regex-test
  (testing "matches valid absolute IRIs"
    (is (re-matches AbsoluteIRIRegEx "http://foo.com"))
    (is (not (re-matches AbsoluteIRIRegEx "foo.com")))
    (is (not (re-matches AbsoluteIRIRegEx "www.foo.com")))
    ;; See issue 17 https://github.com/yetanalytics/xapi-schema/issues/17
    (is (re-matches AbsoluteIRIRegEx "foo:/a"))
    (is (re-matches AbsoluteIRIRegEx "foo+bar.baz-quxx:/a"))
    (is (re-matches AbsoluteIRIRegEx "reallydamnlongschemeoverhere://foo.bar")))
  (testing "matches IRIs with fragments"
    (is (re-matches AbsoluteIRIRegEx "http://example.com/xapi/verbs#sent-a-statement"))
    (is (re-matches AbsoluteIRIRegEx "http://example.com/xapi/foo/#bar?my_jimmies=rustled"))))

(deftest mailto-iri-regex-test
  (testing "matches valid mailto IRIs"
    (is (re-matches MailToIRIRegEx "mailto:milt@yetanalytics.com"))
    (is (not (re-matches MailToIRIRegEx "http://foo.com")))
    (is (not (re-matches MailToIRIRegEx "milt@yetanalytics.com")))
    (is (not (re-matches MailToIRIRegEx "mi%lt@yetanalytics.com")))
    (is (re-matches MailToIRIRegEx "mailto:mi%0Alt@yetanalytics.com"))))

(deftest uuid-regex-test
  (testing "matches valid 4.0 UUIDs"
    (is (re-matches UuidRegEx "f47ac10b-58cc-4372-a567-0e02b2c3d479"))
    (is (not (re-matches UuidRegEx "1234-1234-1234-1234")))
    (is (not (re-matches UuidRegEx "3c7db14d-ac4b-4e35-b2c6-3b2237f382")))
    (is (not (re-matches UuidRegEx "MA97B177-9383-4934-8543-0F91A7A02836")))))

(deftest timestamp-regex-test
  (testing "matches valid ISO 8601 datetime stamps within the rfc3339 profile"
    (is (re-matches TimestampRegEx "2015-05-13T15:16:00Z"))
    (is (re-matches TimestampRegEx "2015-05-13T15:16:00.304Z"))
    (is (re-matches TimestampRegEx "2015-05-13T15:16:00-20:00"))
    (is (re-matches TimestampRegEx "2016-11-22T16:50:25.3868080Z"))
    (is (not (re-matches TimestampRegEx "5-13-2015")))
    (is (not (re-matches TimestampRegEx "20150513T15Z")))
    (is (not (re-matches TimestampRegEx "20150513T15:16:00Z")))))

(deftest xapi-version-regex-test
  (testing "matches xAPI 1.0.X versions"
    (is (and (re-matches xAPIVersionRegEx "1.0.0")
             (re-matches xAPIVersionRegEx "1.0.2")))
    (is (not (re-matches xAPIVersionRegEx "0.9.5")))))

(deftest duration-regex-test
  (testing "matches ISO durations"
    (is (re-matches DurationRegEx "P3Y6M4DT12H30M5S"))
    (is (re-matches DurationRegEx "P23DT122.34S"))
    (is (not (re-matches DurationRegEx "PT")))
    (is (not (re-matches DurationRegEx "P10.3DT1.7S")))))

(deftest base64-regex-test
  (testing "matches Base64 encoded stuff"
    (is (re-matches Base64RegEx "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a"))
    (is (not (re-matches Base64RegEx "12345")))))

(deftest sha1-regex-test
  (testing "matches SHA-1 hashes"
    (is (re-matches Sha1RegEx "ebd31e95054c018b10727ccffd2ef2ec3a016ee9"))
    (is (not (re-matches Sha1RegEx "1234")))))
