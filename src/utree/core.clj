(ns utree.core
  (:require (utree.dot))
  (:require (clojure [string :as str]))
  (:gen-class))

(defn ^{:cli true :usage "filename"} dot
  "Generate dot files from utility trees"
  [filename]
  (utree.dot/dot filename))

(defn subcommands
  []
  (filter :cli (map (fn [[k v]] (meta v)) (ns-publics 'utree.core))))

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
  (println "Usage: utree [subcommand] command-options")
  (println "\nSubcommands:")
  (doseq [c (subcommands)]
    (apply print (interpose "\t" (map c [:name :usage :doc])))
    (println)))

(defn -main
  ([] (command-help))
  ([cmd & args]
     (if-let [cmdfn (lookup-command cmd)]
       (cmdfn args)
       (println "Unrecognized subcommand" cmd))))

