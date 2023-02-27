(defproject io.biotz/message-schema "0.1.1"
  :url "https://bitbucket.org/magnet-coop/biotz-message-schema"
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [metosin/malli "0.9.2"]
                 [com.widdindustries/cljc.java-time "0.1.21"]]
  :plugins [[s3-wagon-private "1.3.5"]]
  :deploy-repositories
  [["magnet-s3-repo" {:url "s3p://mvn-private-repository/releases/"
                      :no-auth true
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
                           :plugins [[jonase/eastwood "1.3.0"]
                                     [lein-cljfmt "0.8.0"]]}}
  :test-selectors {:default (fn [m] (not (or (:integration m) (:regression m))))
                   :all (constantly true)
                   :integration :integration
                   :regression :regression}
  :repl-options {:init-ns io.biotz.message-schema.core})
