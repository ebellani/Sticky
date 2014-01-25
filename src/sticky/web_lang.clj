(ns sticky.web-lang
  "The language that deals with web pages and urls. Use this to fetch
  documents from the web, slice and dice them, paginate and so
  forth."
  (:require [org.httpkit.client :as http])
  (:import  [org.jsoup Jsoup]
            [org.jsoup.select Selector]))

(defonce
  #^{:dynamic true
     :doc "Used for the with-response macro and variants. It is this
     var that will be exposed to any functions and macros that handle
     responses."}
  *current-response* nil)

(defonce
  #^{:dynamic true
     :doc "Used for the with-document macro and variants. It is this
     var that will be exposed to any functions that handle
     documents."}  *current-document* nil)

(defn select-elements
  "Uses the Jsoup selector syntax [1] to query elements in the document or root element node passed.
[1] http://jsoup.org/apidocs/org/jsoup/select/Selector.html"
([query] (select-elements query *current-document*))
([query element] (Selector/select query element)))

(defmacro with-response [url http-method args & body]
  "Builds a connection, executes it, and exposes the response to the
body."
  `(binding [*current-response* @(~http-method ~url ~args)]
     ~@body))

(defmacro with-document* [url http-method args & body]
  "Builds a connection, executes it, parse the response and exposes the
document to the body."
  `(with-response ~url ~http-method ~args
     (binding [*current-document* (Jsoup/parse (:body *current-response*)
                                               ~url)]
       ~@body)))

(defmacro with-document [url & body]
  "Default options."
  `(with-document* ~url  http/get {:follow-redirects false}
     ~@body))
