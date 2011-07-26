(ns dot-utility.core
  (:use (dot-utility dot parser))
  (:require (clojure [string :as str]))
  (:gen-class))

(defn file->dot-string [f]
  (with-out-str
    (emit-dot (file->graph f))))

(defn -main [& args]
  (doseq [f args]
    (println (file->dot-string f))))
