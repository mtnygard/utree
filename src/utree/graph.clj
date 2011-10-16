(ns utree.graph
  (:require (clojure [set :as set])))

(defn add-node [g n] (if (g n) g (assoc g n {:next #{} :prev #{}})))
(defn set-node-attributes ([g n & avs] (assoc g n (apply assoc (g n) avs))))
(defn get-node-attribute [g n a] ((g n) a))

(defn add-edge [g n1 n2]
 (-> g
     (add-node n1)
     (add-node n2)
     (update-in [n1 :next] conj n2)
     (update-in [n2 :prev] conj n1)))

(defn next-nodes [g n] (get-in g [n :next]))
(defn prev-nodes [g n] (get-in g [n :prev]))
(defn nodes [g] (keys g))

(defn initial-graph
  "Create a graph with just the top-level 'Utility' node"
  []
  (-> {}
      (add-node 0)
      (set-node-attributes 0 :level 0 :label "Utility" :rank 1)))

(defn graph-by-ranks
  "Return a map of ranks (levels) in the graph, where each value is a sequence of the [node attributes] pairs at that level"
  [g]
  (group-by (fn [[k v]] (:level v)) g))

(defn- inv
  "Invert each element in the collection"
  [coll]
  (map #(/ 1.0 %) coll))

(defn roc
  "Compute the rank ordered centroid of rank n out of k options"
  [n k]
  (/ (reduce + (inv (range n (inc k)))) k))

(defn assign-roc-weight
  [graph node cohort parent-weight]
  (if-let [rank (get-node-attribute graph node :rank)]
    (let [roc-weight (* parent-weight (roc rank cohort))
          children (next-nodes graph node)]
      (reduce (fn [graph child]
                (assign-roc-weight graph child (count children) roc-weight))
              (assoc-in graph [node :weight] roc-weight)
              children))
    graph))

(defn assign-roc-weights
  [g]
  (assign-roc-weight g 0 1 1.0))

(defn find-node
  "Find a node by the sequence of labels from root."
  ([g lseq]
     (if-let [nodes (find-node g [0] lseq)]
       (first nodes)
       (do
         (println "WARNING: Could not locate quality attribute for label '" lseq "'")
         nil)))
  
  ([g nodes lseq]
     (if-let [child-label (first lseq)]
       (find-node g 
                  (filter #(= child-label (get-node-attribute g % :label)) (apply set/union (map #(next-nodes g %) nodes)))
                  (rest lseq))
       nodes)))
