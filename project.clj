(defproject rock-paper-siccors "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljfmt "0.6.5"]]
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 [com.cemerick/pomegranate "1.1.0"]
                 [clj-http "3.10.0"]
                  [cheshire "5.9.0"]
                 ]
  :repl-options {:init-ns rock-paper-siccors.core})
