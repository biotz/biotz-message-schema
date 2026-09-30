(defproject io.biotz/message-schema "0.1.24-SNAPSHOT"
  :url "https://github.com/biotz/biotz-message-schema"
  :description "Biotz IoT Message schema (and meta-schema) validation and decoding library"
  :license {:name "Mozilla Public Licence 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :dependencies [[org.clojure/clojure "1.12.6"]
                 [metosin/malli "0.20.2"]
                 [com.widdindustries/cljc.java-time "0.1.22"]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]]
  :test-paths ["test"]
  :profiles {:dev [:project/dev :profiles/dev]
             :repl {:prep-tasks ^:replace ["javac" "compile"]
                    :repl-options {:init-ns user
                                   :host "0.0.0.0"
                                   :port 4001}}
             :profiles/dev {}
             :project/dev {:eastwood {:linters [:all]
                                      :exclude-linters [:keyword-typos
                                                        :boxed-math
                                                        :non-clojure-file
                                                        :performance
                                                        :unused-namespaces
                                                        :unused-locals]
                                      :debug [:progress :time]}
                           :resource-paths ["dev/resources"]
                           :source-paths ["dev/src"]
                           :dependencies [[criterium/criterium "0.4.6"]]
                           :plugins [[jonase/eastwood "1.4.3"]
                                     [dev.weavejester/lein-cljfmt "0.13.0"]]}}
  :test-selectors {:default (fn [m] (not (or (:integration m) (:regression m))))
                   :all (constantly true)
                   :integration :integration
                   :regression :regression}
  :repl-options {:init-ns io.biotz.message-schema.core})
