(defproject codenames "0.1.0-SNAPSHOT"
  :description "Play Codenames online!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[clj-time "0.14.0"]
                 [com.rpl/specter "1.0.3"]
                 [com.taoensso/sente "1.11.0"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [metosin/compojure-api "2.0.0-alpha7"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"
                  :scope "provided"]
                 [org.immutant/immutant-web "1.1.4"]
                 [org.immutant/scheduling "2.1.9"]
                 [reagent "0.7.0"]
                 [reagent-utils "0.2.1"]
                 [ring "1.6.2"]
                 [ring-server "0.4.0"]
                 [ring/ring-defaults "0.3.1"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.0"
                  :exclusions [org.clojure/tools.reader]]
                 [yogthos/config "0.9"]]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.5"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler codenames.handler/app
         :uberwar-name "codenames.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "codenames.jar"

  :main codenames.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to        "target/cljsbuild/public/js/app.js"
              :output-dir       "target/cljsbuild/public/js"
              :source-map       "target/cljsbuild/public/js/app.js.map"
              :optimizations :advanced
              :pretty-print  false
              :preloads [re-frisk.preload]}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "codenames.core/mount-root"}
             :compiler
             {:main "codenames.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true}}}}


  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                      ]
   :css-dirs ["resources/public/css"]
   :ring-handler codenames.handler/app}



  :profiles {:dev {:repl-options {:init-ns codenames.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[binaryage/devtools "0.9.4"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [figwheel-sidecar "0.5.13"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [pjstadig/humane-test-output "0.8.2"]
                                  [prone "1.1.4"]
                                  [re-frisk "0.5.0"]
                                  [ring/ring-devel "1.6.2"]
                                  [ring/ring-mock "0.3.1"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.13"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
