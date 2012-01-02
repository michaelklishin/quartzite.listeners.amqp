(ns clojurewerkz.quartzite.listeners.amqp.PublishingSchedulerListener
  (:gen-class :implements   [org.quartz.SchedulerListener]
              :init         init
              :state        state
              :constructors {[com.rabbitmq.client.Channel String String] []})
  (:require [langohr.basic     :as lhb]
            [clojure.data.json :as json])
  (:import [org.quartz SchedulerListener]
           [com.rabbitmq.client Channel]
           [java.util Date]
           [clojurewerkz.quartzite.listeners.amqp PublishingSchedulerListener]))



(defn publish
  [^PublishingSchedulerListener this payload ^String type]
  (let [{ :keys [channel exchange routing-key] } @(.state this)
        payload (json/json-str payload)]
    (lhb/publish channel exchange routing-key payload :type type)))


(defn -init
  [^Channel ch ^String exchange ^String routing-key]
  [[] (atom { :channel ch :exchange exchange :routing-key routing-key })])


(defmacro payloadless-publisher
  [method-name message-type]
  `(defn ~method-name
     [this#]
     (publish this# (json/json-str {}) ~message-type)))

(payloadless-publisher -schedulerStarted       "quartz.scheduler.started")
(payloadless-publisher -schedulerInStandbyMode "quartz.scheduler.standby")
(payloadless-publisher -schedulingDataCleared  "quartz.scheduler.cleared")
(payloadless-publisher -schedulerShuttingDown  "quartz.scheduler.shutdown")
