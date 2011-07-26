(ns dot-utility.graph)


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
