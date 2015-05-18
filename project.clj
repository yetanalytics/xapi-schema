(defproject xapi-schema "0.1.0-SNAPSHOT"
  :description "Clojure(script) Schema for the Experience API v1.0.3"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [prismatic/schema "0.4.2"]
                 [cheshire "5.4.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]]
  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]]
  :plugins [[lein-cljsbuild "1.0.6-SNAPSHOT"]
            [speclj "3.2.0"]]
  :profiles {:dev {:dependencies [[speclj "3.2.0"]]
                   :plugins [[com.keminglabs/cljx "0.6.0" :exclusions [org.clojure/clojure]]]}}

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes/clj"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/classes/cljs"
                   :rules :cljs}
                  {:source-paths ["spec/cljx"]
                   :output-path "target/spec/clj"
                   :rules :clj}
                  {:source-paths ["spec/cljx"]
                   :output-path "target/spec/cljs"
                   :rules :cljs}]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["target/classes/cljs"]
                        :compiler {:output-to "target/js/xapi_schema_dev.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       {:id "test"
                        :source-paths ["target/classes/cljs" "target/spec/cljs" "spec/clj"]
                        :compiler {:output-to "target/js/xapi_schema_test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}
                        :notify-command ["phantomjs" "bin/speclj" "target/js/xapi_schema_test.js"]}
                       {:id "test-browser"
                        :source-paths ["target/classes/cljs"]
                        :compiler {:output-to "resources/public/xapi_schema.js"
                                   :optimizations :advanced}}
                       {:id "release"
                        :source-paths ["target/classes/cljs"]
                        :compiler {:output-to "target/js/xapi_schema.js"
                                   :optimizations :advanced}}]
              :test-commands {"test" ["phantomjs"  "bin/speclj" "target/js/xapi_schema_test.js"]}}
  :test-paths ["target/classes/clj" "target/spec/clj" "spec/clj"]
  :prep-tasks [["cljx" "once"] "javac" "compile"])
