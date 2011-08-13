(ns utree.core
  (:use (clojure.java io))
  (:require (utree [dot :as dot]
                   [html :as html]))
  (:require (clojure [string :as str]))
  (:gen-class))

(defmacro defcommand
  [name args&opts docstring bodyfn]
  `(defn ~name ~docstring {:cli true :usage ~args&opts} [args#]
     (apply ~bodyfn args#)))

(defmacro defcommands
  [& command-defs]
  `(do ~@(map (fn [command-def]
                `(defcommand ~@command-def)) command-defs)))

(defcommands
  (dot "filename" "Generate dot files from utility trees" dot/dot)
  (html "filename" "Generate an HTML ordered list representing the tree" html/html-from-utility-tree))

(defn lookup-command
  [ns name]
  (if-let [cmdvar (ns-resolve ns (symbol name))]
    (if (:cli (meta cmdvar))
      cmdvar)))

(defn bad-subcommand 
  [name]
  (fn [args]
    (println "Unrecognized subcommand" name)))

(defn subcommands
  [ns]
  (filter :cli (map (fn [[k v]] (meta v)) (ns-publics ns))))

(defn command-help [ns]
  (println "Usage: utree [subcommand] command-options")
  (println)
  (doseq [c (subcommands ns)]
    (println (interpose "\t" (map c [:name :args&opts :doc])))
    (println)))

(defn -main
  ([] (command-help 'utree.core))
  ([cmd & args]
      (let [cmdfn (or (lookup-command 'utree.core cmd) (bad-subcommand cmd))]
        (cmdfn args))))

