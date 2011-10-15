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

(defn section-header?
  [line]
  (< 1 (count (take-while #(= \- %) line))))

(defn section-type [[line]] (keyword (str/lower-case (str/trim (str/replace line #"^-+\s*" "")))))

(defn split-sections [lines]
  (partition-by section-header? lines))

(defmulti parse-section (fn [world type lines] type))
(defmethod parse-section :utility
  [world name lines]
  (into world (hash-map :utility (lines->graph lines))))

(defmethod parse-section :alternatives
  [world name lines]
  (into world {:alternatives '()}))

(defn parse-world [sects]
  (reduce
   (fn [world [header body]] (parse-section world (section-type header) body))
   {}
   (partition 2 sects))
)

(defn parse-file [filename]
  (-> filename
      (slurp)
      (str/split #"\n")
      (split-sections)
      (parse-world)))
