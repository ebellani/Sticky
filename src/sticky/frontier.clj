(ns sticky.frontier
  "The Web connection. Agents that collect pages."
  (:require [http.async.client :as c]))

(defn create-fetchers [number-of-agents agent-size dump-fun]
  "Creates a number of fetchers that will save the http client into a
  vector and adds to each of them a watcher that observes when the
  agent vector grows beyond a given limit to call the dump function
  with the vector.
,
  The return vector contains all the agents, with one of them in a
  pair with the marker keyword, indicating that this should be the
  one to be used next."
  (let [agnt-initial-state []
        agnts (for [n (range number-of-agents)]
                (let [current-agent (agent agnt-initial-state)
                      watcher-function (fn [k agnt old-state new-state]
                                         (when (> (count new-state)
                                                  agent-size)
                                           (dump-fun agnt new-state)
                                           ;; restart the agnt state
                                           (send-off agnt
                                                     (fn [x & args]
                                                       agnt-initial-state))))]
                  (add-watch current-agent :dumper-watcher watcher-function)
                  current-agent))]
    (vec (conj (rest agnts)
               [:current (first agnts)]))))


(defmacro send-to-a-fetcher [fetchers function & args]
  "Receive the fetchers and the message and args to be sent to one of
  them. Send the message to the :current one and change the current
  value to the next agent. This is a macro in order to splice the args"
  `(let [[new-fetchers# current-fetcher#] (get-next-current ~fetchers)]
     (send-off current-fetcher# ~function ~@args)
     new-fetchers#))

(defn get-next-current [a-seq]
  "Given a sequence of objects, find a pair whose first element is
  the :current, returns a pair with the second element and a sequence
  with the assigned marker to the next element in the original
  sequence, in a cyclic way. "
  (when a-seq
    (loop [i 0]
      (cond (>= i (count a-seq))
            (throw (Exception. "Did not find the :current marker."))
            (and (coll? (a-seq i))
                 (= (first (a-seq i)) :current))            
            (let [current-element (second (a-seq i))
                  next-i (mod (inc i) (count a-seq))
                  cleaned-seq (assoc a-seq i current-element)]
              [(assoc cleaned-seq next-i [:current (a-seq next-i)])
               current-element])
            :else (recur (inc i))))))


;; (loop [fetchers (create-fetchers 10 3 println)
;;        n     0]
;;   (when (< n 100)
;;     (recur (send-to-a-fetcher fetchers conj "ping")
;;            (inc n))))