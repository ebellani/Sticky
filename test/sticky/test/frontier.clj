(ns sticky.test.frontier
  (:use [sticky.frontier])
  (:use [clojure.test]))

(deftest next-test
  ;;(is (= 10 (count (create-fetchers 10 3 (fn [x] (println x))))))
  (is (= [[1 2 [:current 3]] 2] (get-next-current [1 [:current 2] 3])))
  (is (= [[[:current 1] 2 3] 3] (get-next-current [1 2 [:current 3]])))
  (is (thrown? Exception (get-next-current [1 2 3]))))

(deftest fetchers-test
  (let [fetchers (create-fetchers 10 3 (fn [x] (println x)))]
    (is (= 10 (count fetchers)))
    (is (= (first (first fetchers)) :current))
    ;; change the current flag to the next fetcher
    ;; should print the message too.
    (is (= (first (second (send-to-a-fetcher fetchers conj "A message.")))
           :current))
    (await-for 4000)))
