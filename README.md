[![Build Status](https://travis-ci.org/yetanalytics/xapi-schema.svg?branch=master)](https://travis-ci.org/yetanalytics/xapi-schema)

![Clojars Project](http://clojars.org/com.yetanalytics/xapi-schema/latest-version.svg)

# xapi-schema

Clojure(script) schema for Experience API 1.0.3. Provides validation of Statements and other xAPI objects.

## Demo

You can use xapi-schema to validate statements in real time [with this demo.](http://yetanalytics.github.io/xapi-schema-demo/)

## Getting Started
1. Add to your project dependencies: `[[com.yetanalytics/xapi-schema "0.1.0-SNAPSHOT"]]`
2. Require:
```clojure
(ns your-project.core
  (:require [xapi-schema.core :as xs]
            [xapi-schema.schemata.json :as json]))
```

## Usage
### Clojure(script)
#### Validate a Statement or Statements in EDN
```clojure
(def statement
  {"id" "fd41c918-b88b-4b20-a0a5-a4c32391aaa0"
   "actor" {"objectType" "Agent"
            "name" "Project Tin Can API"
            "mbox" "mailto:user@example.com"}
   "verb" {"id" "http://example.com/xapi/verbs#sent-a-statement",
           "display" {"en-US" "sent"}}
   "object" {"id" "http://example.com/xapi/activity/simplestatement",
             "definition"
             {"name" {"en-US" "simple statement"}
              "description"
              {"en-US" "A simple Experience API statement. Note that the LRS
                does not need to have any prior information about the Actor (learner), the
                verb, or the Activity/object."}}}})

(xs/validate-statement-data statement) ;; => returns the statement

(xs/validate-statement-data [statement statement statement]) ;; => returns the statements

(let [bad-statement (dissoc statement "actor")]
  (xs/validate-statement-data bad-statement)) ;; => throws Exception or js/Error
```
#### Validate a Statement from JSON (Clojurescript)
```clojure
(let [json-statement (clj->js statement)]
  (xs/validate-statement-data-js json-statement)) ;; => returns the statement
```
#### Validate a Statement from a JSON string (clojure(script))
```clojure
(def statement-str
  "{\"object\":{\"id\":\"http://example.com/xapi/activity/simplestatement\",
  \"definition\":{\"name\":{\"en-US\":\"simple statement\"},\"description\":
  {\"en-US\":\"A simple Experience API statement. Note that the LRS\\n
  does not need to have any prior information about the Actor (learner), the\\n
  verb, or the Activity/object.\"}}},\"verb\":{\"id\":\"http://example.com/xapi
  /verbs#sent-a-statement\",\"display\":{\"en-US\":\"sent\"}},\"id\":\"fd41c918-
  b88b-4b20-a0a5-a4c32391aaa0\",\"actor\":{\"mbox\":\"mailto:user@example.com\"
  ,\"name\":\"Project Tin Can API\",\"objectType\":\"Agent\"}}")

(xs/validate-statement-data statement-str) ;; => returns statement edn
```

#### 'Check' a Statement
Checking a statement will return nill if it is valid, or a map of errors.

```clojure
(xs/statement-checker statement) ;; => nil
(let [bad-statement (-> statement
                        (dissoc "actor")
                        (assoc "id" 123)]
  (xs/statement-checker bad-statement)))
  ;; => (named {"actor" missing-required-key,
  ;;            "id" (named (not (instance? java.lang.String 123)) "Uuid")} "Statement")
```
#### Coerce Errors to Data
You can use the `errors->data` fn to coerce errors into nested data that looks like your statement
```clojure
(let [bad-statement (-> statement
                        (dissoc "actor")
                        (assoc "id" 123)]
  (xs/errors->data (xs/statement-checker bad-statement))))
  ;; => {"actor" "Missing", "id" "Not a string: 123"}
```

#### Use SubSchemata
All of the subschemata in `xapi-schema.schemata.json` are valid [Prismatic Schemas](https://github.com/Prismatic/schema):
```clojure
(ns your-project.core
  (:require [xapi-schema.core :as xs]
            [xapi-schema.schemata.json :as json]
            [schema.core :as s]))
(s/check json/Agent {"mbox" "mailto:bob@example.com"}) ;; => nil
```
### Plain 'ol JS
If you want to use validations from JavaScript, first build the js:
`$ lein do cljx, cljsbuild once release`
And then include the generated file, `target/js/xapi_schema.js` and invoke:
```javascript
var statement-str = '{"id":"fd41c918-b88b-4b20-a0a5-a4c32391aaa0", "actor":{"objectType": "Agent","name":"Project Tin Can API","mbox":"mailto:user@example.com"},"verb":{"id":"http://example.com/xapi/verbs#sent-a-statement","display":{ "en-US":"sent" }},"object":{"id":"http://example.com/xapi/activity/simplestatement","definition":{"name":{ "en-US":"simple statement" },"description":{ "en-US":"A simple Experience API statement. Note that the LRS does not need to have any prior information about the Actor (learner), the verb, or the Activity/object." }}}}';
var statement-json = JSON.parse(s);
xapi_schema.core.validate_statement_data_js(statement-str); // => statement JSON
xapi_schema.core.validate_statement_data_js(statement-json); // => statement JSON
```
## Testing
### Clojure
`$ lein do cljx, spec`
or
`$ lein spec-clj` (alias)
### ClojureScript
`$ lein do cljx, cljsbuild once test`
or
`$ lein spec-cljs` (alias)
## License

Copyright © 2015 Yet Analytics, Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
