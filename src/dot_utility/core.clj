(ns dot-utility.core
  (:require (clojure [string :as str]))
  (:gen-class))

(let [id-sequence (atom 0)]
  (defn make-node [t l] {:id (str "node" (dosync (swap! id-sequence inc))) :level t :label l}))

(defn add-node [g n] (if (g n) g (assoc g n {:next #{} :prev #{}})))

(defn add-edge [g n1 n2]
 (-> g
     (add-node n1)
     (add-node n2)
     (update-in [n1 :next] conj n2)
     (update-in [n2 :prev] conj n1)))

(defn next-nodes [g n] (get-in g [n :next]))
(defn prev-nodes [g n] (get-in g [n :prev]))
(defn nodes [g] (keys g))

(defn split-lines [str] (filter (comp not empty?) (str/split str #"\n")))

(defn nodify
  [line]
  (let [stars (count (take-while #(= \* %) line))]
    (if (> stars 0) (make-node stars (clojure.string/trim (subs line stars))))))

(defn insert-root [nodes] (conj nodes (make-node 0 "Utility")))

(defn parent-at-level [level g node]
  (when node
      (if (= level (:level node)) node (recur level g (first (prev-nodes g node))))))

(defn graphify [nodeseq]
  (reduce (fn [g [n1 n2]]
            (add-edge g (parent-at-level (dec (:level n2)) g n1) n2))
          {}
          (partition 2 1 nodeseq)))

(defn emit-rank
  [[r nodes]]
  (println "subgraph {\nrank=same;")
  (doseq [n nodes]
    (println (str (:id n) " [label=\"" (:label n) "\"];")))
  (println "}"))

(defn emit-dependencies
  [g]
  (doseq [n (nodes g)
          trg (next-nodes g n)]
    (println (str (:id n) "->" (:id trg) ";"))))

(defn emit-dot
  [g]
  (println "digraph {
     rankdir=LR;
     node[shape=\"plaintext\"];
     edge[arrowhead=\"none\"];")
  (doall (map emit-rank (sort (group-by :level (keys g)))))
  (emit-dependencies g)
  (println "}"))

(defn -main [& args]
  (doseq [f args]
    (->> f
         (slurp)
         (split-lines)
         (map nodify)
         (filter (comp not nil?))
         (insert-root)
         (graphify)
         (emit-dot))))
