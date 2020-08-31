(defproject clojure-telegram-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
    :dependencies [[org.clojure/clojure "1.10.1"]
                   [dev.meinside/clogram "0.1.0"]
                   [clj-http "3.10.0"]
                   [cheshire "5.9.0"]
                   [enlive "1.1.6"]
                   [org.clojure/java.jdbc "0.7.11"]
                   [mysql/mysql-connector-java "8.0.21"]]
  :main ^:skip-aot clojure-telegram-bot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
