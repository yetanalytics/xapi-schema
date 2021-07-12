(ns xapi-schema.spec.regex-test
  (:require
   [clojure.test :refer [deftest is testing] :include-macros true]
   [xapi-schema.spec.regex :refer [LanguageTagRegEx
                                   AbsoluteIRIRegEx
                                   RelativeIRLRegEx
                                   AbsoluteURIRegEx
                                   RelativeURLRegEx
                                   MailToIRIRegEx
                                   UuidRegEx
                                   TimestampRegEx
                                   xAPIVersionRegEx
                                   DurationRegEx
                                   Base64RegEx
                                   Sha1RegEx
                                   Sha2RegEx
                                   OpenIdRegEx]]))

(deftest language-tag-regex-test
  (testing "matches valid Language Tags"
    (is (re-matches LanguageTagRegEx "en"))
    (is (re-matches LanguageTagRegEx "arb"))
    (is (re-matches LanguageTagRegEx "en-US"))   ; lang + region
    (is (re-matches LanguageTagRegEx "es-419"))  ; lang + region code
    (is (re-matches LanguageTagRegEx "zh-yue"))  ; lang + extlang
    (is (re-matches LanguageTagRegEx "uz-Arab")) ; lang + script
    (is (re-matches LanguageTagRegEx "zh-cmn-Latn-CN"))     ; lang + extension + region
    (is (re-matches LanguageTagRegEx "zh-Latn-CN-pinyin"))  ; lang + script + region + variant
    (is (re-matches LanguageTagRegEx "de-DE-u-co-phonebk")) ; lang + region + extension
    (is (re-matches LanguageTagRegEx "en-US-x-twain"))      ; lang + region + private
    (is (re-matches LanguageTagRegEx "sl-rozaj-biske")) ; lang + variant
    (is (re-matches LanguageTagRegEx "de-CH-1901"))     ; lang + region + variant
    (is (re-matches LanguageTagRegEx "i-enochian"))     ; grandfathered tag
    (is (re-matches LanguageTagRegEx "foo")) ; doesn't check if tag is registered
    (is (not (re-matches LanguageTagRegEx "not a language tag")))
    (is (not (re-matches LanguageTagRegEx "en-")))
    (is (not (re-matches LanguageTagRegEx "de-419-DE"))) ; two region tags
    (is (not (re-matches LanguageTagRegEx "a-DE")))
    (is (not (re-matches LanguageTagRegEx "fr-u-o")))
    (is (not (re-matches LanguageTagRegEx "americanenglish")))))

(deftest open-id-regex-test
  (testing "matches valid URIs"
    (is (re-matches OpenIdRegEx "http://www.foo.com"))
    (is (not (re-matches OpenIdRegEx "www.foo.com")))
    (is (not (re-matches OpenIdRegEx "foo.com")))
    (is (not (re-matches OpenIdRegEx "hey dude wat")))
    (is (not (re-matches OpenIdRegEx "custom://www.foo.com"))))
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
    (is (re-matches AbsoluteIRIRegEx "http://example.com/xapi/foo/#bar?my_jimmies=rustled"))
    (is (re-matches AbsoluteIRIRegEx "http://a_b/#foo"))
    (is (re-matches AbsoluteIRIRegEx "https://foo-baz.app.com/xapi/def/emb/qux*ROOT"))
    (is (re-matches AbsoluteIRIRegEx "https://foo-baz.app.com/xapi#foo:bar")))
  (testing "matches IRIs with URL encodings"
    (is (re-matches AbsoluteIRIRegEx "http://cenariovr.com/174/Sharks!/sharks-Type%20of%20Shark"))
    (is (re-matches AbsoluteIRIRegEx "http://cenar%20iovr.com/1%2074/S%20harks!/shark%20s-Type%20of%20Shark")))
  (testing "matches IRIs with Unicode characters"
    (is (re-matches AbsoluteIRIRegEx "https://en.wiktionary.org/wiki/Ῥόδος"))
    (is (re-matches AbsoluteIRIRegEx "https://www.你好世界.cn"))
    (is (re-matches AbsoluteIRIRegEx "https://www.example.kr#안녕하세요")))
  (testing "matches IRIs with absolute and rootless paths"
    (is (re-matches AbsoluteIRIRegEx "https:/foo/bar"))
    (is (re-matches AbsoluteIRIRegEx "urn:example:animal:ferret:nose"))))

(deftest relative-irl-regex-test
  (testing "matches valid relative IRLs"
    (is (re-matches RelativeIRLRegEx "/"))
    (is (re-matches RelativeIRLRegEx "/xapi"))
    (is (not (re-matches RelativeIRLRegEx "https://foo.com")))
    (is (not (re-matches RelativeIRLRegEx "www.foo.com"))))
  (testing "matches relative IRLs with fragments"
    (is (re-matches RelativeIRLRegEx "/xapi/verbs#sent-a-statement"))
    (is (re-matches RelativeIRLRegEx "/xapi/foo/#bar?my_jimmies=rustled"))
    (is (re-matches RelativeIRLRegEx "/#foo"))
    (is (re-matches RelativeIRLRegEx "/xapi/def/emb/qux*ROOT"))
    (is (re-matches RelativeIRLRegEx "/xapi#foo:bar")))
  (testing "matches relative IRLs with Unicode characters"
    (is (re-matches RelativeIRLRegEx "/wiki/Ῥόδος")))
  (testing "does not match network path and no-scheme relative IRLs"
    (is (not (re-matches RelativeIRLRegEx "./this:that")))
    (is (not (re-matches RelativeIRLRegEx "foo")))
    (is (not (re-matches RelativeIRLRegEx "//foo.com/bar")))))

(deftest absolute-uri-regex-test
  (testing "matches valid absolute URIs"
    (is (re-matches AbsoluteURIRegEx "http://foo.com"))
    (is (re-matches AbsoluteURIRegEx "http://cenariovr.com/174/Sharks!/sharks-Type%20of%20Shark"))
    (is (not (re-matches AbsoluteURIRegEx "https://en.wiktionary.org/wiki/Ῥόδος")))))

(deftest absolute-url-regex-test
  (testing "matches valid relative URLs"
    (is (re-matches RelativeURLRegEx "/"))
    (is (re-matches RelativeURLRegEx "/#foo"))
    (is (not (re-matches RelativeURLRegEx "/wiki/Ῥόδος")))))

(deftest mailto-iri-regex-test
  (testing "matches valid mailto IRIs"
    (is (re-matches MailToIRIRegEx "mailto:milt@yetanalytics.com"))
    (is (not (re-matches MailToIRIRegEx "http://foo.com")))
    (is (not (re-matches MailToIRIRegEx "milt@yetanalytics.com")))
    (is (not (re-matches MailToIRIRegEx "mi%lt@yetanalytics.com")))
    (is (re-matches MailToIRIRegEx "mailto:mi%0Alt@yetanalytics.com"))
    (is (re-matches MailToIRIRegEx "mailto:foo-baz.@some-domain.com"))
    ;; International email addresses are not yet supported
    (is (not (re-matches MailToIRIRegEx "mailto:你好世界@Ῥόδος.com")))))

(deftest uuid-regex-test
  (testing "matches valid 4.0 UUIDs"
    (is (re-matches UuidRegEx "f47ac10b-58cc-4372-a567-0e02b2c3d479"))
    (is (re-matches UuidRegEx "f47ac10b-58cc-4372-0567-0e02b2c3d479"))
    (is (not (re-matches UuidRegEx "1234-1234-1234-1234")))
    (is (not (re-matches UuidRegEx "3c7db14d-ac4b-4e35-b2c6-3b2237f382")))
    (is (not (re-matches UuidRegEx "MA97B177-9383-4934-8543-0F91A7A02836"))))
  (testing "matches all UUID versions"
    (is (re-matches UuidRegEx "f47ac10b-58cc-1372-0567-0e02b2c3d479"))
    (is (re-matches UuidRegEx "f47ac10b-58cc-2372-0567-0e02b2c3d479"))
    (is (re-matches UuidRegEx "f47ac10b-58cc-3372-0567-0e02b2c3d479"))
    (is (re-matches UuidRegEx "f47ac10b-58cc-4372-0567-0e02b2c3d479"))))

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
    (is (not (re-matches TimestampRegEx "2008-09-15T15:53:00.601-00:00")))))

(deftest xapi-version-regex-test
  (testing "matches xAPI 1.0.X versions"
    (is (and (re-matches xAPIVersionRegEx "1.0.0")
             (re-matches xAPIVersionRegEx "1.0.2")
             (re-matches xAPIVersionRegEx "1.0")
             (re-matches xAPIVersionRegEx "1.0.32-abc.def+ghi.jkl")))
    (is (not (re-matches xAPIVersionRegEx "0.9.5")))))

(deftest duration-regex-test
  (testing "matches ISO durations"
    (is (re-matches DurationRegEx "P3Y6M4DT12H30M5S"))
    (is (re-matches DurationRegEx "P23DT122.34S"))
    (is (re-matches DurationRegEx "PT3H0M25.51S"))
    (is (re-matches DurationRegEx "PT3H25.51S"))
    (is (re-matches DurationRegEx "P0003-06-04T12:30:05")) ; Wikipedia example
    (is (not (re-matches DurationRegEx "PT")))
    (is (not (re-matches DurationRegEx "P10.3DT1.7S")))))

(deftest base64-regex-test
  (testing "matches Base64 encoded stuff"
    (is (re-matches
         Base64RegEx
         "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a"))
    (is (re-matches Base64RegEx "1234abcd"))
    (is (re-matches Base64RegEx "1234abc="))
    (is (re-matches Base64RegEx "1234ab=="))
    (is (re-matches Base64RegEx "1234////"))
    (is (not (re-matches Base64RegEx "12345")))))

(deftest sha1-regex-test
  (testing "matches SHA-1 hashes"
    (is (re-matches Sha1RegEx "ebd31e95054c018b10727ccffd2ef2ec3a016ee9"))
    (is (not (re-matches Sha1RegEx "1234")))))

(deftest sha2-regex-test
  (testing "matches SHA-2 hashes"
    (is (re-matches Sha2RegEx "495395e777cd98da653df9615d09c0fd6bb2f8d4788394cd53c56a3bfdcd848a"))
    (is (not (re-matches Sha2RegEx "Q3lxN0R1NQ==")))))
