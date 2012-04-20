(ns utree.core
  (:require (utree.dot))
  (:require (clojure [string :as str])
            (clojure [pprint :only (cl-format)]))
  (:gen-class))

(defn ^{:cli true :usage "filename"} dot
  "Generate dot files from utility trees"
  [filename]
  (utree.dot/dot filename))

(defn ^{:cli true :usage ""} help
  "Display help message"
  []
  (command-help))

(defn subcommands
  []
  (filter :cli (map meta (vals (ns-publics 'utree.core)))))

(defn find-first
  [pred coll]
  (first (filter pred coll)))

(defn var-named?
  [n v]
  (= (:name v) (symbol n)))

(defn lookup-command
  [name]
  (find-first (partial var-named? name) (subcommands)))

(defn command-help
  []
  (println "Usage: utree subcommand [command-options]")
  (println "\nSubcommands:")
  (doseq [c (subcommands)]
    (clojure.pprint/cl-format true "~10A~30A~40A~%" (:name c) (:usage c) (:doc c)))
  (println))

(defn -main
  ([] (command-help))
  ([cmd & args]
     (if-let [cmdfn (lookup-command cmd)]
       (cmdfn args)
       (println "Unrecognized subcommand" cmd))))

