(ns dot-utility.core
  (:use (dot-utility graph dot))
  (:require (clojure [string :as str]))
  (:gen-class))

(let [id-sequence (atom 0)]
  (defn next-id [] (dosync (swap! id-sequence inc))))

(defn split-lines [str] (filter (comp not empty?) (str/split str #"\n")))

(defn quality-attribute?
  [line]
  (.startsWith line "*"))

(defn parse-line
  [line]
  (let [stars (count (take-while #(= \* %) line))
        rank (second (first (re-seq #"\s*\[([0-9]+)\]\s*" line)))
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

(defn graphify [nodemap]
  (reduce (fn [g [k1 k2]]
            (let [parent-level (dec (get-node-attribute g k2 :level))]
              (add-edge g (parent-at-level parent-level g k1) k2)))
          nodemap
          (partition 2 1 (sort (keys nodemap)))))

(defn root-node []
  (-> {}
      (add-node 0)
      (set-node-attributes 0 :level 0 :label "Utility")))

(defn file->graph [f]
  (->> f
       (slurp)
       (split-lines)
       (filter quality-attribute?)
       (reduce nodify (root-node))
       (graphify)))

(defn text->dot-string [f]
  (with-out-str
    (emit-dot (file->graph f))))

(defn -main [& args] (doseq [f args] (println  (text->dot-string f))))
