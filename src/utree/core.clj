(ns utree.core
  (:require (utree.dot)
            (utree [dot :as dot]
                   [graph :as g]
                   [parser :as p]
                   [radial :as r]
                   [solution :as s]
                   ))
  (:require (clojure [string :as str])
            (clojure [pprint :only (cl-format)]))
  (:gen-class))

(defn is-command?     [v]         (:cli (meta v)))
(defn commands        []          (filter is-command? (vals (ns-publics 'utree.core))))

(defn find-first      [pred coll] (first (filter pred coll)))

(defn var-named?
  [n]
  (fn [v] (= (:name (meta v)) (symbol n))))

(defn lookup-command
  [name]
  (find-first (var-named? name) (commands)))

(defn command-help
  []
  (println "Usage: utree command [command-options]")
  (println "\nCommands:")
  (doseq [{n :name u :usage d :doc} (map meta (commands))]
    (clojure.pprint/cl-format true "~15A~20A~60A~%" n u d))
  (println))

(defn -main
  ([] (command-help))
  ([cmd & args]
     (if-let [cmdfn (lookup-command cmd)]
       (apply cmdfn args)
       (println "Unrecognized command" cmd))))

(defn assoc-roc-weights
  [world]
  (assoc world :utility (g/assign-roc-weights (:utility world))))

(defn emit-dot
  [world]
  (dot/emit-dot (:utility world))
  world)

(defn emit-radar
  [idx soln]
  (r/write-radar-display-to-file (s/solution-scores soln) (str "solution_" idx ".png")))

(defn emit-radars
  [world]
  (doall (map-indexed emit-radar (:alternatives world)))
  world)

(defn emit-ranking
  [world]
  )

(defn ^{:cli true :usage "filename"} dot
  "Generate dot files from utility trees"
  [filename]
  (-> filename
      p/parse-file
      assoc-roc-weights
      emit-dot))

(defn ^{:cli true :usage ""} help
  "Display help message"
  []
  (command-help))

(defn ^{:cli true :usage "filename"} radar-plots
  "Generate radar plots as PNGs named 'solution_0.png' through 'solution_n.png'"
  [filename]
  (->
   filename
   p/parse-file
   emit-radars))

(defn ^{:cli true :usage "filename"} full-report
  "Create a full report: utility tree, radar plots, and alternatives"
  [filename]
  (->
   filename
   p/parse-file
   assoc-roc-weights
   emit-dot
   emit-radars
   emit-ranking))

