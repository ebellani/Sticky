(ns sticky.test.fetchers
  (:use [sticky.fetchers]
        [clojure.test]
        [sticky.utils]))

(deftest next-test
  ;;(is (= 10 (count (create-fetchers 10 3 (fn [x] (println x))))))
  (is (= [[1 2 [:current 3]] 2] (get-next-current [1 [:current 2] 3])))
  (is (= [[[:current 1] 2 3] 3] (get-next-current [1 2 [:current 3]])))
  (is (thrown? Exception (get-next-current [1 2 3]))))

(deftest fetchers-test
  (let [fetchers (create-fetchers 3 2 print)]
    (is (= 3 (count fetchers)))
    (is (= (first (first fetchers)) :current))

    (let [[marker fetcher] (first fetchers)]
      (binding [*out* (java.io.StringWriter.)]
        (is (= @fetcher []))
        (send-to-a-fetcher fetchers conj "A message 1.")
        (send-to-a-fetcher fetchers conj "A message 2.")
        (await-for 1000 fetcher)
        (is (= @fetcher ["A message 1." "A message 2."]))
        (is (= (.toString *out*) ""))
        (send-to-a-fetcher fetchers conj "A message 3.")
        (await-for 1000 fetcher)
        (is (= (.toString *out*)
               "[A message 1. A message 2. A message 3.]"))
        (await-for 1000 fetcher)
        (is (= @fetcher []))))))
