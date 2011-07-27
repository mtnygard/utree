(ns dot-utility.graph-test
  (:use dot-utility.graph)
  (:use midje.sweet))

(defn empty-graph [] {})
(defn one-node-subgraph [id] {id {:prev #{} :next #{}}})
(defn with-attribute [g n a v] (assoc-in g [n a] v))

(def basic-graph (one-node-subgraph "node0"))
(def small-real-graph
     {0 {:label "Utility" :level 0 :rank 1 :next #{1 2}}
      1 {:label "Performance" :level 1 :rank 1 :next #{3 4 5} :prev #{0}}
      2 {:label "Security" :level 1 :rank 2 :next #{6 7} :prev #{0}}
      3 {:label "Response Time" :level 2 :rank 1 :next #{8} :prev #{1}}
      4 {:label "Capacity" :level 2 :rank 2 :next #{9} :prev #{1}}
      5 {:label "Throughput" :level 2 :rank 3 :next #{10} :prev #{1}}
      6 {:label "Confidentiality" :level 2 :rank 1 :next #{11} :prev #{2}}
      7 {:label "Integrity" :level 2 :rank 2 :next #{12} :prev #{2}}
      8 {:label "Under normal load, search requests complete in < 100 ms" :level 3 :rank 1 :next #{} :prev #{3}}
      9 {:label "Under normal load, the server can process 1000 simultaneous requests" :level 3 :rank 1 :next #{} :prev #{4}}
      10 {:label "Under normal load, the server can process 10000 requests per second" :level 3 :rank 1 :next #{} :prev #{5}}
      11 {:label "Data in transit shall be encrypted" :level 3 :rank 1 :next #{} :prev #{6}}
      12 {:label "Transmissions shall include a message authentication code (MAC) to verify the sender" :level 3 :rank 1 :next #{} :prev #{7}}})

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

(facts "about graph ranks"
       (graph-by-ranks small-real-graph) =>
       {0 [[0 {:label "Utility", :level 0, :rank 1, :next #{1 2}}]]
        1 [[1 {:label "Performance", :level 1, :rank 1, :next #{3 4 5}, :prev #{0}}]
           [2 {:label "Security", :level 1, :rank 2, :next #{6 7}, :prev #{0}}]]
        2 [[3 {:label "Response Time", :level 2, :rank 1, :next #{8}, :prev #{1}}]
           [4 {:label "Capacity", :level 2, :rank 2, :next #{9}, :prev #{1}}]
           [5 {:label "Throughput", :level 2, :rank 3, :next #{10}, :prev #{1}}]
           [6 {:label "Confidentiality", :level 2, :rank 1, :next #{11}, :prev #{2}}]
           [7 {:label "Integrity", :level 2, :rank 2, :next #{12}, :prev #{2}}]],
        3 [[8 {:label "Under normal load, search requests complete in < 100 ms", :level 3, :rank 1, :next #{}, :prev #{3}}]
           [9 {:label "Under normal load, the server can process 1000 simultaneous requests", :level 3, :rank 1, :next #{}, :prev #{4}}]
           [10 {:label "Under normal load, the server can process 10000 requests per second", :level 3, :rank 1, :next #{}, :prev #{5}}]
           [11 {:label "Data in transit shall be encrypted", :level 3, :rank 1, :next #{}, :prev #{6}}]
           [12 {:label "Transmissions shall include a message authentication code (MAC) to verify the sender", :level 3, :rank 1, :next #{}, :prev #{7}}]]})

(facts "about rank ordered centroids"
      (roc 1 3) => (roughly 0.6111)
      (roc 2 3) => (roughly 0.2778)
      (roc 3 3) => (roughly 0.1111))
