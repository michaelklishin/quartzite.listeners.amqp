(defproject clojurewerkz/quartzite.listeners.amqp "1.0.0-SNAPSHOT"
  :description "Quartz listeners that publish event messages over AMQP. Intended to be used with clojurewerkz/quartzite."
  :dependencies [[org.clojure/clojure     "1.3.0"]
                 [clojurewerkz/quartzite  "1.0.0-SNAPSHOT"]
                 [com.novemberain/langohr "1.0.0-SNAPSHOT"]
                 [org.clojure/data.json   "0.1.2" :exclude [org.clojure/clojure]]]
  :dev-dependencies [[clj-time                  "0.3.3" :exclusions [org.clojure/clojure]]
                     [org.clojure/tools.logging "0.2.3" :exclusions [org.clojure/clojure]]
                     [org.slf4j/slf4j-simple    "1.6.2"]
                     [org.slf4j/slf4j-api       "1.6.2"]
                     [log4j                     "1.2.16" :exclusions [javax.mail/mail
                                                                      javax.jms/jms
                                                                      com.sun.jdmk/jmxtools
                                                                      com.sun.jmx/jmxri]]]  
  :aot [clojurewerkz.quartzite.listeners.amqp.PublishingSchedulerListener]
  :source-path        "src/clojure"
  :java-source-path   "src/java"
  :dev-resources-path "test/resources"
  :warn-on-reflection true)
