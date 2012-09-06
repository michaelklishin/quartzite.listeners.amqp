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
           [java.util.concurrent ConcurrentLinkedQueue CountDownLatch]))


(sched/initialize)

(defonce ^Connection conn    (lhc/connect))
(defonce ^Channel    channel (.createChannel conn))


(defn message-types-in
  [^ConcurrentLinkedQueue mbox]
  (map (fn [[meta payload]]
         (:type meta)) mbox))

(defn register-consumer
  [^String test-name]
  (let [queue      (str "clojurewerkz.quartzite.test.listeners.amqp." (gensym test-name))
        _          (lhq/declare channel queue :auto-delete true)
        mbox       (ConcurrentLinkedQueue.)
        msg-handler   (fn [ch metadata payload]
                        (.add mbox [metadata payload]))]
    (-> (Thread. #(lhcons/subscribe channel queue msg-handler :auto-ack true)) .start)
    [queue mbox]))


(deftest test-scheduler-started-event
  (let [[queue mbox] (register-consumer "test-scheduler-started-event")
        listener     (PublishingSchedulerListener. channel "" queue)]
    (sched/add-scheduler-listener listener)
    (sched/start)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (is (some #{"quartz.scheduler.started"} (message-types-in mbox)))))


(deftest test-scheduler-cleared-event
  (let [[queue mbox] (register-consumer "test-scheduler-cleared-event")
        listener     (PublishingSchedulerListener. channel "" queue)]
    (sched/add-scheduler-listener listener)
    (sched/clear!)
    (sched/start)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (is (some #{"quartz.scheduler.cleared"} (message-types-in mbox)))))


(deftest test-scheduler-is-put-into-standby-mode-event
  (let [[queue mbox] (register-consumer "test-scheduler-is-put-into-standby-event")
        listener     (PublishingSchedulerListener. channel "" queue)]
    (sched/add-scheduler-listener listener)
    (sched/start)
    (sched/standby)
    (sched/start)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (is (= ["quartz.scheduler.started" "quartz.scheduler.standby" "quartz.scheduler.started"] (vec (message-types-in mbox))))))



;;
;; job.scheduled
;;

(def latch1 (CountDownLatch. 10))

(defrecord Latch1Job []
  org.quartz.Job
  (execute [this ctx]
    (.countDown ^CountDownLatch latch1)))

(deftest test-job-scheduled-event
  (let [[queue mbox] (register-consumer "test-scheduled-event")
        listener     (PublishingSchedulerListener. channel "" queue)
        job     (j/build
                 (j/of-type Latch1Job)
                 (j/with-identity "clojurewerkz.quartzite.listeners.amqp.test.scheduler_lifecycle_events" "job1"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/with-repeat-count 10)
                                    (s/with-interval-in-milliseconds 200))))]
    (sched/add-scheduler-listener listener)
    (sched/start)
    (sched/schedule job trigger)
    (.await latch1)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (is (some #{"quartz.scheduler.job-scheduled"} (vec (message-types-in mbox))))))


;;
;; job.unscheduled
;;

(def latch2 (CountDownLatch. 10))

(defrecord Latch2Job []
  org.quartz.Job
  (execute [this ctx]
    (.countDown ^CountDownLatch latch2)))

(deftest test-job-unscheduled-event
  (let [[queue mbox] (register-consumer "test-job-unscheduled-event")
        listener     (PublishingSchedulerListener. channel "" queue)
        tk      (t/key "clojurewerkz.quartzite.listeners.amqp.test.scheduler_lifecycle_events" "trigger2")
        job     (j/build
                 (j/of-type Latch2Job)
                 (j/with-identity "clojurewerkz.quartzite.listeners.amqp.test.scheduler_lifecycle_events" "job2"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-identity tk)
                  (t/with-schedule (s/schedule
                                    (s/with-repeat-count 10)
                                    (s/with-interval-in-milliseconds 200))))]
    (sched/add-scheduler-listener listener)
    (sched/start)
    (sched/schedule job trigger)
    (.await latch2)
    (Thread/sleep 200)    
    (sched/unschedule-job tk)
    (Thread/sleep 1000)
    (is (not (.isEmpty mbox)))
    (is (some #{"quartz.scheduler.job-unscheduled"}
           (vec (message-types-in mbox))))))



;;
;; job.deleted
;;

(def latch3 (CountDownLatch. 10))

(defrecord Latch3Job []
  org.quartz.Job
  (execute [this ctx]
    (.countDown ^CountDownLatch latch3)))

(deftest test-job-deleted-event
  (let [[queue mbox] (register-consumer "test-job-deleted-event")
        listener     (PublishingSchedulerListener. channel "" queue)
        job     (j/build
                 (j/of-type Latch3Job)
                 (j/with-identity "clojurewerkz.quartzite.listeners.amqp.test.scheduler_lifecycle_events" "job3"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/with-repeat-count 10)
                                    (s/with-interval-in-milliseconds 200))))
        jk      (j/key "clojurewerkz.quartzite.listeners.amqp.test.scheduler_lifecycle_events" "job3")]
    (sched/add-scheduler-listener listener)
    (sched/start)
    (sched/schedule job trigger)
    (.await latch3)
    (Thread/sleep 200)    
    (sched/delete-job jk)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (let [types (vec (message-types-in mbox))]
      (is (some #{"quartz.scheduler.job-deleted"} types))
      (is (some #{"quartz.scheduler.job-added"} types)))))


;;
;; trigger.paused, trigger.finalized
;;

(def latch4 (CountDownLatch. 10))

(defrecord Latch4Job []
  org.quartz.Job
  (execute [this ctx]
    (.countDown ^CountDownLatch latch4)))

(deftest test-trigger-paused-event
  (let [[queue mbox] (register-consumer "test-trigger-paused-event")
        listener     (PublishingSchedulerListener. channel "" queue)
        tk      (t/key "clojurewerkz.quartzite.listeners.amqp.test.scheduler_lifecycle_events" "trigger4")
        job     (j/build
                 (j/of-type Latch3Job)
                 (j/with-identity "clojurewerkz.quartzite.listeners.amqp.test.scheduler_lifecycle_events" "job4"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-identity tk)
                  (t/with-schedule (s/schedule
                                    (s/with-repeat-count 10)
                                    (s/with-interval-in-milliseconds 200))))]
    (sched/add-scheduler-listener listener)
    (sched/start)
    (sched/schedule job trigger)
    (.await latch3)
    (Thread/sleep 200)    
    (sched/pause-trigger tk)
    (Thread/sleep 500)
    (is (not (.isEmpty mbox)))
    (let [types (vec (message-types-in mbox))]
      (is (some #{"quartz.scheduler.trigger-paused"} types))
      (is (some #{"quartz.scheduler.trigger-finalized"} types)))))
