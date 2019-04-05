(defproject pharmacy_counting "0.1.0-SNAPSHOT"
  :description "Pharmacy Data Engineering Interview project"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.443"]
                 [narkisr/cliopatra "1.1.0"]
                 [clj-time "0.15.1"]
                 [environ "1.0.2"]
                 [iota "1.1.3"]]
  :main pharmacy-counting.core
  :jvm-opts ["-Xmx4g" "-Xms512m"]
  :aliases {"launch" ["run" "./input/itcont.txt"]}
  :repositories [["java.net" "https://download.java.net/maven/2"]]
  :profiles {:uberjar {:aot :all}}
  :resource-paths ["input/"
                   "output/"])
