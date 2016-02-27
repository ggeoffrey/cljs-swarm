(defproject swarm "0.1.0-SNAPSHOT"
  :description "A swarm intelligence prototype using Clojure and THREE.JS"
  :url "https://github.com/ggeoffrey/cljs-swarm"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]
            [codox "0.8.15"]]
  :source-paths ["src"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel { :on-jsload "swarm.core/on-js-reload" }
                        :compiler {:main swarm.core
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/swarm.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true }}
                       {:id "min"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/compiled/swarm.js"
                                   :main swarm.core
                                   :externs ["resources/public/libs/three.min.js"
                                             "resources/public/libs/stats.min.js"
                                             "resources/public/libs/OrbitControls.js"]
                                   ;:optimizations :whitespace
                                   :optimizations :advanced
                                   :pretty-print false
                                   :closure-warnings {:externs-validation :off}}}]}
  :figwheel {:css-dirs ["resources/public/css"] ;; watch and update CSS
             ;; :nrepl-port 7888
             ;; :ring-handler hello_world.server/handler
             ;; :repl false
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             }
  :codox {:language :clojurescript
          :src-dir-uri "https://github.com/ggeoffrey/cljs-swarm/tree/master/"}
  :defaults {:doc/format :markdown})
