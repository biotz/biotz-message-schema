(defproject io.biotz/message-schema "0.1.21"
  :url "https://bitbucket.org/magnet-coop/biotz-message-schema"
  :dependencies [[org.clojure/clojure "1.12.5"]
                 [metosin/malli "0.20.1"]
                 [com.widdindustries/cljc.java-time "0.1.22"]]
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
