(defproject com.yetanalytics/xapi-schema "0.1.5-SNAPSHOT"
  :description "Clojure(script) Schema for the Experience API v1.0.3"
  :url "https://github.com/yetanalytics/xapi-schema"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.28"]
                 [prismatic/schema "0.4.3"]
                 [cheshire "5.5.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.taoensso/tower "3.1.0-beta3"]]
  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]]
  :plugins [[lein-cljsbuild "1.0.6"]]
  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}

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
