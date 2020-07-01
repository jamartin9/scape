
(defproject scape "0.1.0-SNAPSHOT"
  :description "advent of code"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.10.2-alpha1"]
                 [etaoin "0.3.6"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                ]
  :main ^:skip-aot scape.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/clojure "1.10.2-alpha1"]]}
             })
