(defproject com.yetanalytics/xapi-schema "0.1.9"
  :description "Clojure(script) Schema for the Experience API v1.0.3"
  :url "https://github.com/yetanalytics/xapi-schema"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [prismatic/schema "1.1.0"]
                 [cheshire "5.6.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.taoensso/tower "3.1.0-beta3"]
                 [slingshot "0.12.2"]]
  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]]

  :plugins [[lein-cljsbuild "1.1.3"]]

  :profiles {:dev {:dependencies [[speclj "3.3.2"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:output-to "target/js/xapi_schema_dev.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       {:id "release"
                        :source-paths ["src"]
                        :compiler {:output-to "target/js/xapi_schema.js"
                                   :optimizations :advanced}}]}
  :resource-paths ["resources"]
  :test-paths ["spec" "dev"]
  :aliases {"deploy-lib" ["do" "clean," "deploy" "clojars"]
            "spec-cljs" ["do" "clean," "run" "-m" "xapi-schema.dev.cljs"]
            "spec-clj" ["do" "run" "-m" "xapi-schema.dev.spec"]
            "ci"   ["do" "spec-clj," "spec-cljs"]})
