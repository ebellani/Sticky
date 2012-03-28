(ns sticky.web-lang
  "The language that deals with web pages and urls. Use this to fetch
  documents from the web, slice and dice them, paginate and so
  forth."
  (:require [clj-http.client :as client]))

(def
  #^{:dynamic true
     :doc "Used as the default response for a request."}
  *response* nil)

(def
  #^{:dynamic true
     :doc "Used as the page for a GET request."}
  *page* nil)

(defmacro with-response [request-fn url & body]
  "Binds the *response* to the return of the fn applied on the url and
exposes it to any expression on the body."
  `(binding [*response* (~request-fn ~url)]
     ~@body))

(defmacro with-page [url & body]
  "Do the usual, GETs a page and exposes it."
  `(with-response ~client/get ~url
     (binding [*page* (:body *response*)]
       ~@body)))

(with-page "http://www.dreamsongs.com/DailyPoems.html"
  (println *page*))
