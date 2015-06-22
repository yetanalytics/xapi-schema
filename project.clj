(defproject com.yetanalytics/xapi-schema "0.1.3-SNAPSHOT"
  :description "Clojure(script) Schema for the Experience API v1.0.3"
  :url "https://github.com/yetanalytics/xapi-schema"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC2"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [prismatic/schema "0.4.3"]
                 [cheshire "5.5.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.taoensso/tower "3.1.0-beta3"]]
  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [speclj "3.3.0"]]
  :profiles {:dev {:dependencies [[speclj "3.3.0"]]}}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src" "spec"]
                        :compiler {:output-to "target/js/xapi_schema_dev.js"
                                   :optimizations :whitespace
                                   :pretty-print true}
                        :notify-command ["phantomjs" "bin/speclj" "target/js/xapi_schema_dev.js"]
                        }
                       {:id "test-browser"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/xapi_schema.js"
                                   :optimizations :advanced}}
                       {:id "release"
                        :source-paths ["src"]
                        :compiler {:output-to "target/js/xapi_schema.js"
                                   :optimizations :advanced}}]
              :test-commands {"test" ["phantomjs"  "bin/speclj" "target/js/xapi_schema_dev.js"]}}
  :resource-paths ["resources"]
  :test-paths ["spec" "dev"]
  :aliases {"cljs" ["do" "clean," "run" "-m" "xapi-schema.dev.cljs"]
            "spec" ["do" "run" "-m" "xapi-schema.dev.spec"]
            "ci"   ["do" "spec," "cljs"]}
  ;; :aliases {"deploy-lib" ["deploy" "clojars"]
  ;;           "spec-clj" ["do" "clean," "spec"]
  ;;           "spec-cljs" ["do" "clean," "cljsbuild" "once" "test"]}
  )
