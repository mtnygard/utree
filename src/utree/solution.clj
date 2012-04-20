(ns utree.solution)

(defn conj-in [m k nv]
  (assoc m k (conj (k m) nv)))

(defn as-int [x]
  (cond 
   (integer? x) x
   (string? x) (Integer/parseInt x)
   (float? x) (int x)
   :else nil))

(defn make-solution [title] {:title title})
(defn solution-title [soln] (:title soln))

(defn add-solution-description [soln desc] (assoc soln :description desc))
(defn add-solution-score [soln attribute score-val score-max] (conj-in soln :scores [attribute (as-int score-val) (as-int score-max)]))

(defn solution-description [soln] (:description soln))
(defn solution-scores [soln] (sort-by first (:scores soln)))

