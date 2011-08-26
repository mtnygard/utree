(require '[cljs.closure :as closure])

(if (= 1 (count *command-line-args*))
  (closure/build (first *command-line-args*)
                 {:optimizations :simple
                  :output-dir "templates/out"
                  :output-to "templates/main.js"})
  (println "compile_js.clj requires exactly one argument, the path to the cljs file to compile"))
