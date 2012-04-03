(ns sticky.test.web-lang
  (:use [sticky.web-lang]
        [clojure.test])
  (:import [org.jsoup Jsoup Connection]))

(def test-url "http://www.dreamsongs.com/DailyPoems.html")

(deftest build-connection-test
  (let [cookies {"a" "1"
                 "xxx" "true"}
        data    {"x" "1.23"
                 "y" "99"}
        headers  {"Accept-Encoding" "gzip,deflate,sdch"
                 "Accept-Charset" "ISO-8859-1,utf-8;q=0.7,*;q=0.3"}
        conn (build-connection test-url
                        {:cookies cookies
                         :data    data
                         :headers  headers
                         :type    :post})
        req (.request conn)]
    (is (= (.toString (.url req)) test-url))
    (is (= (.cookies req) cookies))
    (is (= (.headers req) headers))))

(deftest document-macros-test
  (with-document "http://www.dreamsongs.com/DailyPoems.html"
    (is (= (count (select-elements "p")) 14))))
