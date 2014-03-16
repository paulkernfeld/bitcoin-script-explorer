;; Copyright 2014 Paul Kernfeld. This file is part of bitcoin-script-explorer,
;; which is licensed under the GNU GPL v3. See LICENSE for details.

(defproject com.paulkernfeld/bse "0.1.0"
  :description "Bitcoin Script learning tool"
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :source-paths ["src/cljx"]
  :resource-paths ["src/resources"]
  :test-paths ["target/test-classes"]
  :dependencies [[org.clojure/clojure "1.6.0-alpha1"]
                 [org.clojure/clojurescript "0.0-2156"]]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :clj}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :cljs}]}

  :cljsbuild {:builds {:testable {:source-paths ["target/classes" "target/test-classes"]
                                  :compiler {:output-to "target/cljs/testable.js"
                                             :optimizations :whitespace
                                             :pretty-print true}}
                       :production {:source-paths ["target/classes"]
                                    :compiler {:output-to "app/js/cljs/main.js"
                                               :optimizations :whitespace
                                               :pretty-print true}}}
              :test-commands {"unit-tests" ["phantomjs" :runner
                                            "this.literal_js_was_evaluated=true"
                                            "target/cljs/testable.js"]}}
  
  ;; I think the node.js test runner is broken
  ;; :cljsbuild {:test-commands {"node" ["node" :node-runner "target/testable.js"]}
  ;;             :builds [{:source-paths ["target/classes" "target/test-classes"]
  ;;                       :compiler {:output-to "target/testable.js"
  ;;                                  :libs [""]
  ;;                                  :optimizations :advanced
  ;;                                  :pretty-print true}}]}

  :profiles {:dev {:plugins [[com.cemerick/clojurescript.test "0.2.2"]
                             [com.keminglabs/cljx "0.3.1"]
                             [com.cemerick/austin "0.1.3"]
                             [lein-cljsbuild "1.0.1"]
                             [com.jakemccrary/lein-test-refresh "0.3.4"]]
                   :aliases {"cleantest" ["do" "clean," "cljx" "once," "test,"
                                          "cljsbuild" "once" "testable," "cljsbuild" "test"]
                             "cleanproduction" ["do" "clean," "cljx" "once," "cljsbuild" "once" "production"]}}})
