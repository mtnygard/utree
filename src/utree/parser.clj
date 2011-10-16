(ns utree.parser
  (:use (utree graph))
  (:require (clojure [string :as str])))

;;; Section parser - utility attributes

(let [id-sequence (atom 0)]
  (defn next-id [] (dosync (swap! id-sequence inc))))

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

;;; Structure parser - sections and types

(defn section-type [line]
  (if (re-seq #"^-+" line)
    (-> line
        (str/replace #"^-+" "")
        (str/trim)
        (str/lower-case)
        (keyword))))

(defn split-sections [lines]
  (partition-by section-type lines))

(defn keywordize-headers [coll]
  (let [headers (map (comp section-type first) (take-nth 2 coll))
        bodies (take-nth 2 (drop 1 coll))]
    (map vector headers bodies)))

(def parsers
  {:utility  lines->graph
   :alternatives (fn [body] [])})

(defn parse-world [sects]
  (loop [world {}
         ss (seq sects)]
    (let [[header body] (first ss)]
      (if (and header body)
        (recur (assoc world header ((parsers header) body))
               (next ss))
        world))))

(defn parse-file [filename]
  (-> filename
      (slurp)
      (str/split #"\n")
      (split-sections)
      (keywordize-headers)
      (parse-world)))
