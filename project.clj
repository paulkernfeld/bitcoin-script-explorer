(defproject com.paulkernfeld/bse "0.1.0"
  :description "Bitcoin Script learning tool"
  :url "https://github.com/paulkernfeld/bitcoin-script-explorer"
  :license {:name "GNU GPL v3"
            :url "https://www.gnu.org/copyleft/gpl.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [com.cemerick/clojurescript.test "0.3.0"]] ; in dev
  :profiles {:dev {:source-paths ["src" "dev"]
                   :dependencies [[com.cemerick/clojurescript.test "0.3.0"]]
             :aliases {"cleantest" ["do" "clean," "cljsbuild" "test"]
                       "production" ["do" "cljsbuild" "once" "production"]}}}
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild {:builds
              {:testable {:source-paths ["src/cljs" "test/cljs"]
                          :compiler {:output-to "target/cljs/testable.js"
                                     :optimizations :whitespace
                                     :pretty-print true
                                     :foreign-libs
                                     [{:file "paulkernfeld/bse/js/ripemd160.js"
                                       :provides ["paulkernfeld.bse.js.ripemd160"]}
                                      {:file "paulkernfeld/bse/js/sha256.js"
                                       :provides ["paulkernfeld.bse.js.sha256"]}]}}
               :production {:source-paths ["src/cljs"]
                            :compiler {:output-to "app/js/cljs/main.js"
                                       :optimizations :whitespace
                                       :pretty-print true
                                       :foreign-libs
                                       [{:file "paulkernfeld/bse/js/ripemd160.js"
                                         :provides ["paulkernfeld.bse.js.ripemd160"]}
                                        {:file "paulkernfeld/bse/js/sha256.js"
                                         :provides ["paulkernfeld.bse.js.sha256"]}]}}}
              :test-commands {"unit-tests"
                              ["runners/phantomjs.js"
                               "target/cljs/testable.js"]}})
