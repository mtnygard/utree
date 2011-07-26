(ns dot-utility.graph-test
  (:use dot-utility.graph)
  (:use midje.sweet))

(defn empty-graph [] {})
(defn one-node-subgraph [id] {id {:prev #{} :next #{}}})
(defn with-attribute [g n a v] (assoc-in g [n a] v))

(def basic-graph (one-node-subgraph "node0"))

(facts "about adding nodes to graphs"
       (add-node (empty-graph) "node0") => (contains (one-node-subgraph "node0"))
       (add-node basic-graph "node1") => (contains (one-node-subgraph "node0") (one-node-subgraph "node1"))
       (add-node basic-graph "node0") => (contains (one-node-subgraph "node0")))

(facts "about attributes on nodes"
       (set-node-attributes basic-graph "node0" :k1 'val1) => (contains
                                                               (with-attribute basic-graph "node0" :k1 'val1))
       (set-node-attributes basic-graph "node0" :k1 'val1 :k2 'val2) => (contains
                                                                         (with-attribute 
                                                                           (with-attribute
                                                                             basic-graph "node0" :k1 'val1)
                                                                           "node0" :k2 'val2))
       (let [graph (with-attribute (add-node basic-graph "dijkstra") "dijkstra" :fname "Edsger")]
         (get-node-attribute graph "dijkstra" :fname) => "Edsger"))

(facts "about edges"
       (add-edge (empty-graph) "from" "to") => (contains {"from" {:next #{"to"} :prev #{}} "to" {:next #{} :prev #{"from"}}})
       (next-nodes basic-graph "node0") => #{}
       (next-nodes (add-edge basic-graph "node0" "node1") "node0") => #{"node1"}
       (prev-nodes (add-edge basic-graph "node0" "node1") "node1") => #{"node0"})
