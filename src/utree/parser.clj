(ns utree.parser
  (:use (utree graph))
  (:require (clojure [string :as str])))

(let [id-sequence (atom 0)]
  (defn next-id [] (dosync (swap! id-sequence inc))))

(defn split-lines [str] (filter (comp not empty?) (str/split str #"\n")))

(defn quality-attribute?
  [line]
  (.startsWith line "*"))

(defn parse-int
  [s]
  (try 
    (Integer/parseInt s)
    (catch NumberFormatException _ nil)))

(defn parse-line
  [line]
  (let [stars (count (take-while #(= \* %) line))
        rank (parse-int (second (first (re-seq #"\s*\[([0-9]+)\]\s*" line))))
        label (clojure.string/trim (subs line stars))
        label (clojure.string/replace-first label #"\s*\[([0-9]+)\]\s*" "")]
    [stars label rank]))

(defn nodify
  [g line]
  (let [[level label rank] (parse-line line)
        node (next-id)
        g (add-node g node)]
    (set-node-attributes g node :level level :label label :rank rank)))

(defn parent-at-level [level g node]
  (when node
    (if (= level (get-node-attribute g node :level)) node (recur level g (first (prev-nodes g node))))))

(defn connect-parents [nodemap]
  (reduce (fn [g [k1 k2]]
            (let [parent-level (dec (get-node-attribute g k2 :level))]
              (add-edge g (parent-at-level parent-level g k1) k2)))
          nodemap
          (partition 2 1 (sort (keys nodemap)))))

(defn lines->graph [lines]
  (->> lines
       (filter quality-attribute?)
       (reduce nodify (initial-graph))
       (connect-parents)))

(defn file->graph [f]
  (lines->graph (split-lines (slurp f))))
