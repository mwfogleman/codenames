(defproject codenames "0.1.0-SNAPSHOT"
  :description "Play Codenames online!" ;; Using latest lein reagent +figwheel template
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cljsjs/react "18.2.0-1"]
                 [cljsjs/react-dom "18.2.0-1"]
                 [com.rpl/specter "1.1.4"]
                 [hiccup "1.0.5"]
                 [metosin/reitit "0.6.0"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"
                  :scope "provided"]
                 [pez/clerk "1.0.0"]
                 [reagent "1.2.0"]
                 [reagent-utils "0.3.8"]
                 [ring "1.11.0"]
                 [ring-server "0.5.0"]
                 [ring/ring-defaults "0.4.0"]
                 [venantius/accountant "0.2.5"
                  :exclusions [org.clojure/tools.reader]]
                 [yogthos/config "1.2.0"]]

  :jvm-opts ["-Xmx1G"]

  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [lein-asset-minifier "0.4.6"
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

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  [[:css {:source "resources/public/css/site.css"
          :target "resources/public/css/site.min.css"}]]

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to        "target/cljsbuild/public/js/app.js"
              :output-dir       "target/cljsbuild/public/js"
              :source-map       "target/cljsbuild/public/js/app.js.map"
              :optimizations :advanced
              :infer-externs true
              :pretty-print  false}}
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
              :pretty-print  true}}



            }
   }

  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl
                      ]
   :css-dirs ["resources/public/css"]
   :ring-handler codenames.handler/app}



  :profiles {:dev {:repl-options {:init-ns codenames.repl}
                   :dependencies [[cider/piggieback "0.5.3"]
                                  [binaryage/devtools "1.0.7"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.11.0"]
                                  [prone "2021-04-23"]
                                  [figwheel-sidecar "0.5.20"]
                                  [nrepl "1.1.0"]
                                  [pjstadig/humane-test-output "0.11.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.20"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}


             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
