(ns clojurewerkz.quartzite.test.listeners.amqp.scheduler-lifecycle-events
  (:use [clojure.test])
  (:require [langohr.core      :as lhc]
            [langohr.basic     :as lhb]
            [langohr.queue     :as lhq]
            [langohr.consumers :as lhcons]
            [clojure.data.json :as json]
            [clojurewerkz.quartzite.scheduler :as sched]
            [clojurewerkz.quartzite.jobs      :as j]
            [clojurewerkz.quartzite.triggers  :as t]
            [clojurewerkz.quartzite.schedule.simple :as s]
            [clojurewerkz.quartzite.listeners.amqp.PublishingSchedulerListener])
  (:import [com.rabbitmq.client Connection Channel AMQP$BasicProperties Envelope]
           [clojurewerkz.quartzite.listeners.amqp PublishingSchedulerListener]
           [java.util.concurrent ConcurrentLinkedQueue]))



(defonce ^Connection conn    (lhc/connect))
(defonce ^Channel    channel (.createChannel conn))


(defn message-types-in
  [^ConcurrentLinkedQueue mbox]
  (map (fn [[^Envelope delivery ^AMQP$BasicProperties properties payload]]
         (.getType properties)) mbox))



(deftest test-scheduler-started-event
  (let [queue      "clojurewerkz.quartzite.test.listeners.amqp.test-scheduler-started-event"
        _          (lhq/declare channel queue :auto-delete true)
        mbox       (ConcurrentLinkedQueue.)
        msg-handler   (fn [delivery properties payload]
                        (.add mbox [delivery properties payload]))
        listener   (PublishingSchedulerListener. channel "" queue)]
    (-> (Thread. #(lhcons/subscribe channel queue msg-handler :auto-ack true)) .start)
    (is (.isEmpty mbox))
    (sched/add-scheduler-listener listener)
    (sched/start)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (is (some #{"quartz.scheduler.started"} (message-types-in mbox)))))


(deftest test-scheduler-cleared-event
  (let [queue      "clojurewerkz.quartzite.test.listeners.amqp.test-scheduler-cleared-event"
        _          (lhq/declare channel queue :auto-delete true)
        mbox       (ConcurrentLinkedQueue.)
        msg-handler   (fn [delivery properties payload]
                        (.add mbox [delivery properties payload]))
        listener   (PublishingSchedulerListener. channel "" queue)]
    (-> (Thread. #(lhcons/subscribe channel queue msg-handler :auto-ack true)) .start)
    (is (.isEmpty mbox))
    (sched/add-scheduler-listener listener)
    (sched/clear!)
    (sched/start)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (is (some #{"quartz.scheduler.cleared"} (message-types-in mbox)))))


(deftest test-scheduler-cleared-event
  (let [queue      "clojurewerkz.quartzite.test.listeners.amqp.test-scheduler-cleared-event"
        _          (lhq/declare channel queue :auto-delete true)
        mbox       (ConcurrentLinkedQueue.)
        msg-handler   (fn [delivery properties payload]
                        (.add mbox [delivery properties payload]))
        listener   (PublishingSchedulerListener. channel "" queue)]
    (-> (Thread. #(lhcons/subscribe channel queue msg-handler :auto-ack true)) .start)
    (is (.isEmpty mbox))
    (sched/add-scheduler-listener listener)
    (sched/start)    
    (sched/standby)
    (sched/start)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (is (= ["quartz.scheduler.started" "quartz.scheduler.standby" "quartz.scheduler.started"] (vec (message-types-in mbox))))))
