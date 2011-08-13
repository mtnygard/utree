(ns utree.dot-test
  (:use (utree graph dot))
  (:use midje.sweet))

(facts "about labels"
       (label-for-node {:label "foo"}) => "foo"
       (label-for-node {:label "A long label." :rank 9}) => "A long label."
       (label-for-node {:label "foo" :rank 1 :weight 0.75}) => "foo\\nweight: 0.75"
       (label-for-node {:label "foo" :weight (* 1.0 1/3)}) => "foo\\nweight: 0.33")

(facts "about the top level"
       (with-out-str (emit-dot ...graph...)) => (has-prefix "digraph {")
       (with-out-str (emit-dot ...graph...)) => (contains "rankdir=LR;")
       (with-out-str (emit-dot ...graph...)) => (contains "node[shape=\"plaintext\"];")
       (with-out-str (emit-dot ...graph...)) => (contains "edge[arrowhead=\"none\"];")
       (with-out-str (emit-dot ...graph...)) => (has-suffix "}\n")

       (against-background (emit-ranks anything) => ""
                           (emit-dependencies anything) => ""))

