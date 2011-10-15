(ns utree.dot
  (:use (utree parser graph)))

(defn label-for-node
  [{label :label weight :weight}]
  (if weight (format "%s\\nweight: %3.2f" label weight) label))

(defn emit-rank
  [[r nodes]]
  (println "subgraph {\nrank=same;")
  (doseq [[id attr] nodes]
    (println (str id " [label=\"" (label-for-node attr) "\"];")))
  (println "}"))

(defn emit-ranks
  [g]
  (doall (map emit-rank (graph-by-ranks g))))

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

(defn graph->dot-string
  [g]
  (with-out-str
    (emit-dot
     (assign-roc-weights g))))

(defn dot
  [filename]
  (let [world (parse-file filename)]
    (println (graph->dot-string (:utility world)))))
