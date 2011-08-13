(defproject utree "0.0.2"
  :description "Create a utility tree from simple text format"
  :dependencies [[clojure "1.2.1"]
                 [clojure-contrib "1.2.0"]
                 [org.clojure/data.json "0.1.1"]
                 [fleet "0.9.4"]]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]
                     [midje "1.2-alpha4"]]
  :main utree.core)
