(defproject jkkramer/verily "0.5.0-SNAPSHOT"
  :description "Map validation library"
  :url "https://github.com/jkk/verily"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1843"]]
  :source-paths ["src/cljx"]
  :test-paths ["target/test-classes"]
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
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.0.5"]
                                  [com.keminglabs/cljx "0.3.0"]]
                   :plugins [[com.keminglabs/cljx "0.3.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl
                                                     cljx.repl-middleware/wrap-cljx]}
                   :hooks [cljx.hooks]}})
