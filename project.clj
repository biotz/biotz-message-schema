(defproject io.biotz/message-schema "0.1.20-SNAPSHOT"
  :url "https://bitbucket.org/magnet-coop/biotz-message-schema"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [metosin/malli "0.16.2"]
                 [com.widdindustries/cljc.java-time "0.1.21"]]
  :plugins [[s3-wagon-private "1.3.5"]]
  :deploy-repositories
  [["private-mvn-repo" {:url "s3p://biotz-mvn-private-repository/releases/"
                        :username :env/mvn_private_repo_username
                        :passphrase :env/mvn_private_repo_password
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
                           :dependencies [[criterium "0.4.6"]]
                           :plugins [[jonase/eastwood "1.4.2"]
                                     [dev.weavejester/lein-cljfmt "0.12.0"]]}}
  :test-selectors {:default (fn [m] (not (or (:integration m) (:regression m))))
                   :all (constantly true)
                   :integration :integration
                   :regression :regression}
  :repl-options {:init-ns io.biotz.message-schema.core})
