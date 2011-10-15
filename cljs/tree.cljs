(ns tree
  (:require [cljs-d3.core :as d3]
            [cljs-d3.scale :as scale]
            [cljs-d3.layout :as layout]))

(let [width 960
      height 2000
      i 0
      duration 500
      
      tree (-> d3/d3
               (layout/tree)
               (layout/size height (- width 160)))

      vis (-> d3/d3
              (d3/select "#chart")
              (d3/append "svg:svg")
              (d3/attr {:width width
                        :height height})
              (d3/append "svg:g")
              (d3/attr {:transform "translate(40,0)"}))
      ])
