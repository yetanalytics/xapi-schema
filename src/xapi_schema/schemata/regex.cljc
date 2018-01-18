(ns xapi-schema.schemata.regex)

(def LanguageTagRegEx
  #"^(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)|((en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)))$")

(def OpenIdRegEx
  #?(:clj #"^((((http|https):(?:\/{1,2})?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-\[\]\:\+]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w\-]*))?)$"
     :cljs #"^((((http|https):(?:/{1,2})?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-\[\]\:\+]+)((?:/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w\-]*))?)$"))

(def AbsoluteIRIRegEx
  #?(:clj #"^((([A-Za-z\+\-\.]+:(?:\/{1,2})?)(?:[\-;\*\(\)!\':&=\+\$,\w]+@)?[A-Za-z0-9\.\-\_\[\]\:\+]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\*\.\w_]*)#?(?:[\$\&\'\(\)+,;\.\!\*\/\\\w\-\?=]*))?)$"
     :cljs  #"^((([A-Za-z\+\-\.]+:(?:/{1,2})?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-\_\[\]\:\+]+)((?:/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w\-\?=]*))?)$"))

(def MailToIRIRegEx
  #"mailto:(?:[a-zA-Z0-9!#$&'*+/=?^_`{|}~-]|%[0-9a-fA-F]{2})+(?:\.(?:[a-zA-Z0-9!#$&'*+/=?^_`{|}~-]|%[0-9a-fA-F]{2})+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?")

(def UuidRegEx
  #"[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[4|1][0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}")

(def TimestampRegEx
  #"^(\d{4}(?!\d{2}\b))((-)((0[1-9]|1[0-2])(\3([12]\d|0[1-9]|3[01]))))((T(0[0-9]|1[0-9]|2[0-3])(:([0-4][0-9]|5[0-9])){2}(\.\d+)?)|T23:59:60)(Z|([+-](0[0-9]|1[0-9]|2[0-3])(:([0-4][0-9]|5[0-9]))))$")

;; Based on http://www.regexr.com/39s32
(def xAPIVersionRegEx
  #"^(1\.0|((1\.0\.([0-9]+)(?:-([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?)(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?))$")

(def DurationRegEx
  #"P(?:(?:(?:\d+D|\d+\.\d+D$)|(?:\d+M|\d+\.\d+M$)(?:\d+D|\d+\.\d+D$)?|(?:\d+Y|\d+\.\d+Y$)(:?(?:\d+M|\d+\.\d+M$)(?:\d+D|\d+\.\d+D$)?)?)(:?T(?:(?:\d+H|\d+\.\d+H$)(?:(?:\d+M|\d+\.\d+M$)(?:\d+S|\d+\.\d+S$)?)?|(?:\d+M|\d+\.\d+M$)(?:\d+S|\d+\.\d+S$)?|(?:\d+S|\d+\.\d+S$)))?|T(?:(?:\d+H|\d+\.\d+H$)(?:(?:\d+M|\d+\.\d+M$)(?:\d+S|\d+\.\d+S$)?)?|(?:\d+M|\d+\.\d+M$)(?:\d+S|\d+\.\d+S$)?|(?:\d+S|\d+\.\d+S$))|(?:\d+W|\d+\.\d+W$))")

(def Base64RegEx
  #"^(?:[A-Za-z0-9\+\/]{4})*(?:[A-Za-z0-9\+\/]{2}==|[A-Za-z0-9\+\/]{3}=|[A-Za-z0-9\+\/]{4})$")

(def Sha1RegEx
  #"^[0-9a-fA-F]{40}$")
