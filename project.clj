(defproject clojurewerkz/quartzite.listeners.amqp "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "Quartz listeners that publish event messages over AMQP. Intended to be used with clojurewerkz/quartzite."
  :dependencies [[org.clojure/clojure    "1.4.0"]
                 [clojurewerkz/quartzite "1.0.0"]
                 [com.novemberain/langohr "2.2.1"]
                 [org.clojure/data.json   "0.1.2"]]
  :source-paths ["src/clojure"]
  :profiles {:dev {:resource-paths ["test/resources"],
                   :dependencies   [[clj-time "0.4.4"                  :exclusions [org.clojure/clojure]]
                                    [org.clojure/tools.logging "0.2.3" :exclusions [org.clojure/clojure]]
                                    [org.slf4j/slf4j-simple "1.6.2"]
                                    [org.slf4j/slf4j-api "1.6.2"]
                                    [log4j "1.2.16" :exclusions [javax.mail/mail
                                                                 javax.jms/jms
                                                                 com.sun.jdmk/jmxtools
                                                                 com.sun.jmx/jmxri]]]}
             :1.3 {:resource-paths ["test/resources"],
                   :dependencies   [[org.clojure/clojure "1.3.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-master-SNAPSHOT"]]}}
  :aliases  { "all" ["with-profile" "dev:dev,1.3:dev,1.5"] }
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                               :snapshots true
                               :releases {:checksum :fail :update :always}}}
  :aot [clojurewerkz.quartzite.listeners.amqp.PublishingSchedulerListener]
  :javac-options      ["-target" "1.6" "-source" "1.6"]
  :java-source-paths ["src/java"]
  :warn-on-reflection true)