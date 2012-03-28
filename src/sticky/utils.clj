(ns sticky.utils
  "Utilities."
  (:use [clojure.reflect]
        [clojure.set]
        [clojure.pprint]))


(defn print-methods-table [obj]
  "pprint all methods, parameter, and return types of a object,
sorted by method name."
  (let [columns [:name :parameter-types :return-type]
        reflection (reflect obj)]
    (print-table columns
                 (sort-by :name (project (:members reflection) columns)))
    ;; (println (format "Bases --> %s%nAbove table for class --> %s"
    ;;                  (:bases reflection) obj))
    ))
