(ns dot-utility.dot-test
  (:use (dot-utility graph dot))
  (:use midje.sweet))

(facts "about labels"
       (label-for-node {:label "foo"}) => "foo"
       (label-for-node {:label "bar" :rank 1}) => "bar\\n1"
       (label-for-node {:label "A long label." :rank 9}) => "A long label.\\n9")

(facts "about the top level"
       (with-out-str (emit-dot ...graph...)) => (has-prefix "digraph {")
       (with-out-str (emit-dot ...graph...)) => (contains "rankdir=LR;")
       (with-out-str (emit-dot ...graph...)) => (contains "node[shape=\"plaintext\"];")
       (with-out-str (emit-dot ...graph...)) => (contains "edge[arrowhead=\"none\"];")
       (with-out-str (emit-dot ...graph...)) => (has-suffix "}\n")

       (against-background (emit-ranks anything) => ""
                           (emit-dependencies anything) => ""))

