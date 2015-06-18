(defproject com.yetanalytics/xapi-schema "0.1.2"
  :description "Clojure(script) Schema for the Experience API v1.0.3"
  :url "https://github.com/yetanalytics/xapi-schema"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [prismatic/schema "0.4.3"]
                 [cheshire "5.5.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.taoensso/tower "3.1.0-beta3"]]
  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]]
  :plugins [[lein-cljsbuild "1.0.6"]
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
  :source-paths ["target/classes/clj"]
  :resource-paths ["resources" "target/classes/cljs"]
  :test-paths ["target/classes/clj" "target/spec/clj" "spec/clj"]
  :prep-tasks [["cljx" "once"] "javac" "compile"]
  :aliases {"build-once" ["do" "clean," "cljx" "once"]
            "deploy-lib" ["do" "build-once," "deploy" "clojars"]
            "spec-clj" ["do" "clean," "spec"]
            "spec-cljs" ["do" "clean," "cljx," "cljsbuild" "once" "test"]})
