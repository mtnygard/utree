(ns dot-utility.dot
  (:use dot-utility.graph))

(defn label-for-node
  [{label :label rank :rank}]
  (if rank (str label "\\n" rank) label))

(defn emit-rank
  [[r nodes]]
  (println "subgraph {\nrank=same;")
  (doseq [[id attr] nodes]
    (println (str id " [label=\"" (label-for-node attr) "\"];")))
  (println "}"))

(defn emit-ranks
  [g]
  (doall (map emit-rank (group-by (fn [[k v]] (:level v)) g))))

(defn emit-dependencies
  [g]
  (doseq [n (nodes g)
          trg (next-nodes g n)]
    (println (str n "->" trg ";"))))

(defn emit-dot
  [g]
  (println "digraph {
    rankdir=LR;
    node[shape=\"plaintext\"];
    edge[arrowhead=\"none\"];")
  (emit-ranks g)
  (emit-dependencies g)
  (println "}"))

