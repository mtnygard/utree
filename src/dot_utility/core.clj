(ns dot-utility.core
  (:require (clojure [string :as str]))
  (:gen-class))

(def id-sequence (atom 0))
(defn get-id! [] (str "node" (dosync (swap! id-sequence inc))))

(defn make-node [t l] {:id (get-id!) :level t :label l})

(defn add-node [g n] (if (g n) g (assoc g n {:next #{} :prev #{}})))

(defn add-edge [g n1 n2]
 (-> g
     (add-node n1)
     (add-node n2)
     (update-in [n1 :next] conj n2)
     (update-in [n2 :prev] conj n1)))

(defn contains-node? [g n] (g n))
(defn contains-edge? [g n1 n2] (get-in g [n1 :next n2]))
(defn next-nodes [g n] (get-in g [n :next]))
(defn prev-nodes [g n] (get-in g [n :prev]))
(defn nodes [g] (keys g))

(defn split-lines [str]
  (filter (comp not empty?) (str/split str #"\n")))

(defn nodify
  [line]
  (let [stars (count (take-while #(= \* %) line))
        label (clojure.string/trim (subs line stars))]
    (if (> stars 0) (make-node stars label))))

(defn insert-root [nodes] (conj nodes (make-node 0 "Utility")))

(defn parent-at-level [level g node]
  (when node
      (if (= level (:level node)) node (recur level g (first (prev-nodes g node))))))

(defn graphify [nodeseq]
  (reduce (fn [g [n1 n2]]
            (add-edge g (parent-at-level (dec (:level n2)) g n1) n2))
          {}
          (partition 2 1 nodeseq)))

(defn prolog
  [g]
  (println "digraph {
     rankdir=LR;
     node[shape=\"plaintext\"];
     edge[arrowhead=\"none\"];"))

(defn rank
  [nodes]
  (println "subgraph {\nrank=same;")
  (doseq [n nodes]
    (println (str (:id n) " [label=\"" (:label n) "\"];")))
  (println "}"))

(defn dependencies
  [g]
  (doseq [n (nodes g)
        trg (next-nodes g n)]
    (println (str (:id n) "->" (:id trg) ";"))))

(defn epilog
  [g]
  (println "}"))

(defn ranks-in [g] (set (map :level (nodes g))))

(defn emit-dot
  [g]
  (prolog g)
  (doseq [r (ranks-in g)]
    (rank (filter #(= r (:level %)) (nodes g))))
  (dependencies g)
  (epilog g))

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
