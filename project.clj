(defproject life-fhir-gen "0.1"
  :description "FHIR Test Data Generator"
  :url "https://github.com/life-research/life-fhir-gen"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies
  [[cheshire "5.8.1"]
   [org.clojure/clojure "1.10.0"]
   [org.clojure/tools.cli "0.4.1"]]

  :profiles
  {:uberjar
   {:aot [life-fhir-gen.core]}}

  :main ^:skip-aot life-fhir-gen.core)