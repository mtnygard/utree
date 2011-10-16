(ns utree.solution)

(defn make-solution [title] {:title title})
(defn solution-title [soln] (:title soln))

(defn add-solution-description [soln desc] (assoc soln :description desc))
(defn add-solution-score [soln attribute score] (assoc-in soln [:scores attribute] score))

(defn solution-description [soln] (:description soln))
(defn solution-scores [soln] (:scores soln))
(defn solution-score [soln attribute] (get-in soln [:scores attribute] 0))
