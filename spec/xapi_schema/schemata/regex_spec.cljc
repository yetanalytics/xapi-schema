(ns xapi-schema.schemata.regex-spec
  #?@(:cljs [(:require-macros [speclj.core :refer [describe it should should= should-not run-specs]])
             (:require [speclj.core]
                       [xapi-schema.schemata.regex :refer [LanguageTagRegEx
                                                           URIRegEx
                                                           AbsoluteIRIRegEx
                                                           MailToIRIRegEx
                                                           UuidRegEx
                                                           TimestampRegEx
                                                           xAPIVersionRegEx
                                                           DurationRegEx
                                                           Base64RegEx
                                                           Sha1RegEx]])])
  #?(:clj (:require [speclj.core :refer :all]
                    [xapi-schema.schemata.regex :refer :all])))

(describe "LanguageTagRegex"
          (it "matches valid Language Tags"
              (should (re-matches LanguageTagRegEx "en-US"))
              (should-not (re-matches LanguageTagRegEx "not a language tag"))))

(describe "URIRegex"
          (it "matches valid URIs"
              (should (re-matches URIRegEx "www.foo.com"))
              (should (re-matches URIRegEx "foo.com"))
              (should-not (re-matches URIRegEx "hey dude wat")))
          (it "matches URIs with fragments"
              (should (re-matches URIRegEx "http://example.com/xapi/verbs#sent-a-statement"))))

(describe "AbsoluteIRIRegEx"
          (it "matches valid absolute IRIs"
              (should (re-matches AbsoluteIRIRegEx "http://foo.com"))
              (should-not (re-matches AbsoluteIRIRegEx "foo.com"))
              (should-not (re-matches AbsoluteIRIRegEx "www.foo.com"))
              ;; See issue 17 https://github.com/yetanalytics/xapi-schema/issues/17
              (should (re-matches AbsoluteIRIRegEx "foo:/a"))
              (should (re-matches AbsoluteIRIRegEx "foo+bar.baz-quxx:/a"))
              (should (re-matches AbsoluteIRIRegEx "reallydamnlongschemeoverhere://foo.bar")))
          (it "matches IRIs with fragments"
              (should (re-matches AbsoluteIRIRegEx "http://example.com/xapi/verbs#sent-a-statement"))))

(describe "MailtToIRIRegEx"
          (it "matches valid mailto IRIs"
              (should (re-matches MailToIRIRegEx "mailto:milt@yetanalytics.com"))
              (should-not (re-matches MailToIRIRegEx "http://foo.com"))
              (should-not (re-matches MailToIRIRegEx "milt@yetanalytics.com"))
              (should-not (re-matches MailToIRIRegEx "mi%lt@yetanalytics.com"))
              (should (re-matches MailToIRIRegEx "mailto:mi%0Alt@yetanalytics.com"))))

(describe "UuidRegEx"
          (it "matches valid 4.0 UUIDs"
              (should (re-matches UuidRegEx "f47ac10b-58cc-4372-a567-0e02b2c3d479"))
              (should-not (re-matches UuidRegEx "1234-1234-1234-1234"))
              (should-not (re-matches UuidRegEx "3c7db14d-ac4b-4e35-b2c6-3b2237f382"))))

(describe "TimestampRegex"
          (it "matches valid ISO 8601 datetime stamps"
              (should (re-matches TimestampRegEx "2015-05-13T15:16:00Z"))
              (should-not (re-matches TimestampRegEx "5-13-2015"))))

(describe "xAPIVersionRegEx"
          (it "matches xAPI 1.0.X versions"
              (should (and (re-matches xAPIVersionRegEx "1.0.0")
                           (re-matches xAPIVersionRegEx "1.0.2")))
              (should-not (re-matches xAPIVersionRegEx "0.9.5"))))

(describe "DurationRegEx"
          (it "matches ISO durations"
              (should (re-matches DurationRegEx "P3Y6M4DT12H30M5S"))
              (should (re-matches DurationRegEx "P23DT122.34S"))
              (should-not (re-matches DurationRegEx "PT"))
              (should-not (re-matches DurationRegEx "P10.3DT1.7S"))))

(describe "Base64RegEx"
          (it "matches Base64 encoded stuff"
              (should (re-matches Base64RegEx "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a"))
              (should-not (re-matches Base64RegEx "12345"))))

(describe "Sha1RegEx"
          (it "matches SHA-1 hashes"
              (should (re-matches Sha1RegEx "ebd31e95054c018b10727ccffd2ef2ec3a016ee9"))
              (should-not (re-matches Sha1RegEx "1234"))))
