(ns utree.parser
  (:use (utree graph solution))
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
        label (str/trim (subs line stars))
        label (str/replace-first label #"\s*\[([0-9]+)\]\s*" "")]
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

(defn parse-quality-attributes [world lines]
  (->> lines
       (filter quality-attribute?)
       (reduce nodify (initial-graph))
       (connect-parents)))

;;; Section parser - solution alternatives

(defn solution-title? [line] (.startsWith line "*"))

(defn score-lines [soln] (filter #(re-seq #"^[a-zA-Z/, ]*:\s*[0-9]+/[0-9]+$" %) (solution-description soln)))

(defn quality-label [score-line]
  (-> score-line
      (str/split #":")
      (first)))

(defn quality-score [score-line]
  (-> score-line
      (str/split #":[ \t]*")
      (second)
      (str/split #"/")))

(defn scores-from-description
  [soln world]
  (for [score-line (score-lines soln)]
    (list* (quality-label score-line) (quality-score score-line))))

(defn parse-scores [soln world]
  (loop [soln soln
         scores (scores-from-description soln world)]
    (if-let [[quality-node v mx] (first scores)]
      (recur (add-solution-score soln quality-node v mx)
             (next scores))
      soln)))

(defn parse-solutions
  "Return a seq of solutions."
  [world lines]
  (let [lines (drop-while (comp not solution-title?) lines)]
    (for [[[title-ln] desc] (partition 2 (partition-by solution-title? lines))]
      (-> (make-solution (str/replace title-ln #"^\* " ""))
          (add-solution-description desc)
          (parse-scores world)))))

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
  {:utility      parse-quality-attributes
   :alternatives parse-solutions})

(defn parse-world [sects]
  (loop [world {}
         ss (seq sects)]
    (let [[header body] (first ss)]
      (if (and header body)
        (recur (assoc world header ((parsers header) world body))
               (next ss))
        world))))

(defn parse-file [filename]
  (-> filename
      (slurp)
      (str/split #"\n")
      (split-sections)
      (keywordize-headers)
      (parse-world)))
