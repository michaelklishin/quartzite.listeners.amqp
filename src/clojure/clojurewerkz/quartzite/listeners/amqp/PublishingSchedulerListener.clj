(ns clojurewerkz.quartzite.listeners.amqp.PublishingSchedulerListener
  (:gen-class :implements   [org.quartz.SchedulerListener]
              :init         init
              :state        state
              :constructors {[com.rabbitmq.client.Channel String String] []})
  (:require [langohr.basic     :as lhb]
            [clojure.data.json :as json]
            [clojurewerkz.quartzite.conversion :refer :all])
  (:import [org.quartz SchedulerListener SchedulerException Trigger TriggerKey JobDetail JobKey]
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


(defn -schedulerError
  [this ^String msg ^SchedulerException cause]
  (publish this (json/json-str { :message msg :cause (str cause) }) "quartz.scheduler.error"))


(defn -jobScheduled
  [this ^Trigger trigger]
  (publish this (json/json-str { :group (-> trigger .getKey .getGroup) :key (-> trigger .getKey .getName) :description (.getDescription trigger) }) "quartz.scheduler.job-scheduled"))

(defn -jobUnscheduled
  [this ^TriggerKey key]
  (publish this (json/json-str { :group (.getGroup key) :key (.getName key) }) "quartz.scheduler.job-unscheduled"))

(defn -triggerFinalized
  [this ^Trigger trigger]
  (publish this (json/json-str { :group (-> trigger .getKey .getGroup) :key (-> trigger .getKey .getName) :description (.getDescription trigger) }) "quartz.scheduler.trigger-finalized"))

(defn -triggerPaused
  [this ^TriggerKey key]
  (publish this (json/json-str { :group (.getGroup key) :key (.getName key) }) "quartz.scheduler.trigger-paused"))

(defn -triggersPaused
  [this ^String trigger-group]
  (publish this (json/json-str { :group trigger-group }) "quartz.scheduler.triggers-paused"))

(defn -triggerResumed
  [this ^TriggerKey key]
  (publish this (json/json-str { :group (.getGroup key) :key (.getName key) }) "quartz.scheduler.trigger-resumed"))

(defn -triggersResumed
  [this ^String trigger-group]
  (publish this (json/json-str { :group trigger-group }) "quartz.scheduler.triggers-resumed"))



(defn -jobAdded
  [this ^JobDetail detail]
  (publish this (json/json-str { :job-detail (from-job-data (.getJobDataMap detail)) :description (.getDescription detail) }) "quartz.scheduler.job-added"))

(defn -jobDeleted
  [this ^JobKey key]
  (publish this (json/json-str { :group (.getGroup key) :key (.getName key) }) "quartz.scheduler.job-deleted"))

(defn -jobPaused
  [this ^JobKey key]
  (publish this (json/json-str { :group (.getGroup key) :key (.getName key) }) "quartz.scheduler.job-paused"))

(defn -jobsPaused
  [this ^String job-group]
  (publish this (json/json-str { :group job-group }) "quartz.scheduler.jobs-paused"))

(defn -jobResumed
  [this ^JobKey key]
  (publish this (json/json-str { :group (.getGroup key) :key (.getName key) }) "quartz.scheduler.job-resumed"))

(defn -jobsResumed
  [this ^String job-group]
  (publish this (json/json-str { :group job-group }) "quartz.scheduler.jobs-resumed"))
