(ns sticky.web-lang
  "The language that deals with web pages and urls. Use this to fetch
  documents from the web, slice and dice them, paginate and so
  forth."
  (:use [sticky.utils])
  (:import [org.jsoup Jsoup Connection Connection$Method]
           [org.jsoup.select Selector]))

(defonce
  #^{:dynamic true
     :doc "Used for the with-response macro and variants. It is this
     var that will be exposed to any functions and macros that handles
     responses."}
  *current-response* nil)

(defonce
  #^{:dynamic true
     :doc "Used for the with-document macro and variants. It is this
     var that will be exposed to any functions that handles
     documents."}
  *current-document* nil)

(defmacro set-connection-attribute! [connection attr-symbol map]
  `(doseq [[name# value#] ~map]
    (. ~connection ~attr-symbol name# value#)))

(defn- set-cookies! [connection map]
  (set-connection-attribute! connection cookie map))

(defn- set-data! [connection map]
  (set-connection-attribute! connection data map))

(defn- set-headers! [connection map]
  (set-connection-attribute! connection header map))

(defn- set-user-agent! [])

(defn- set-type! [connection tp]
  (let [request (.request connection)]    
    (cond (= tp :post) (.method request Connection$Method/POST)
          (= tp :get)  (.method request Connection$Method/GET))))

(def dispatch-table

  #^{:doc "Will call the appropriate function for the keyword. All
these functions will mutate the connection object, so they should not
be called individually. Use the build connection function or the
with-connection macro."}

  {:cookies     set-cookies!
   :data        set-data!
   :headers     set-headers!
   :user-agent  set-user-agent!
   :type        set-type!})

(defn build-connection [url {:keys [cookies data header type]
                             :or {type :get}
                             :as args}]
  "Builds a connection for a given URL. The optional arguments are:

:cookies    => map of cookie name -> value pairs
:data       => map of data parameters
:header     => map of headers. String -> string
:user-agent => A string to set the request user-agent header.
:type       => :post or :get. The default is :get. 
"
  (let [connection (Jsoup/connect url)]
    (doseq [[fun-name params] args]
      ((fun-name dispatch-table) connection params))
    connection))

(defn select-elements
  "Uses the Jsoup selector syntax [1] to query elements in the document or root element node passed.
[1] http://jsoup.org/apidocs/org/jsoup/select/Selector.html"
([query] (select-elements query *current-document*))
([query root-node] (Selector/select query *current-document*)))

(defmacro with-response [url args & body]
  "Builds a connection, executes it, and exposes the response to the
body."
  `(binding [*current-response* (.execute (build-connection ~url ~args))]
     ~@body))

(defmacro with-document* [url args & body]
  "Builds a connection, executes it, parse the response and exposes the
document to the body."
  `(with-response ~url ~args
     (binding [*current-document* (.parse *current-response*)]
       ~@body)))

(defmacro with-document [url & body]
  "Default options."
  `(with-document* ~url {:type :get} ~@body))
