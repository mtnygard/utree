(ns utree.html
  (:use (utree parser graph)
        fleet)
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str]))

(def template-path "templates")

(defn dir? [filename] (.isDirectory (io/file filename)))
(defn copy [fromname toname] (io/copy (io/file fromname) (io/file toname)))
(defn mkdir [dirname] (.mkdir (io/file dirname)))

(defn assets [base pred]
  (map (memfn getPath) (filter pred (file-seq (io/file base)))))

(defn replace-path [full from to]
  (str/replace full from to))

(defn graph->nested-list [g n] (list (g n) (map #(graph->nested-list g %) (next-nodes g n))))

(defn copy-assets
  [fromdir todir]
  (doseq [srcdir (assets fromdir dir?)]
    (mkdir (replace-path srcdir fromdir todir)))
  (doseq [srcfile (assets fromdir (complement dir?))]
    (copy srcfile (replace-path srcfile fromdir todir))))

(defn html-from-utility-tree
  [filename outdir]
  "Generates an HTML view of the utility tree into the specified directory.
   Will write several files."
  (mkdir outdir)
  (copy-assets template-path outdir)
  (spit
   (io/file outdir "utility-tree.json")
   (with-out-str
     (json/pprint-json
      (graph->nested-list
       (file->graph filename) 0)))))
