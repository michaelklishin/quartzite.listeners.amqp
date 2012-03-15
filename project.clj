(defproject clojurewerkz/quartzite.listeners.amqp "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "Quartz listeners that publish event messages over AMQP. Intended to be used with clojurewerkz/quartzite."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [clojurewerkz/quartzite "1.0.0-beta2"]
                 [com.novemberain/langohr "1.0.0-beta1"]
                 [org.clojure/data.json "0.1.2"]]
  :source-paths ["src/clojure"]
  :profiles {:dev {:resource-paths ["test/resources"],
                   :dependencies   [[clj-time "0.3.3"                  :exclusions [org.clojure/clojure]]
                                    [org.clojure/tools.logging "0.2.3" :exclusions [org.clojure/clojure]]
                                    [org.slf4j/slf4j-simple "1.6.2"]
                                    [org.slf4j/slf4j-api "1.6.2"]
                                    [log4j "1.2.16" :exclusions [javax.mail/mail
                                                                 javax.jms/jms
                                                                 com.sun.jdmk/jmxtools
                                                                 com.sun.jmx/jmxri]]]}
             :1.4 {:resource-paths ["test/resources"],
                   :dependencies   [[org.clojure/clojure "1.4.0-beta4"]]}}
  :aliases  { "all" ["with-profile" "dev:dev,1.4"] }
  :aot [clojurewerkz.quartzite.listeners.amqp.PublishingSchedulerListener]
  :javac-options      ["-target" "1.6" "-source" "1.6"]
  :java-source-paths ["src/java"]
  :warn-on-reflection true)