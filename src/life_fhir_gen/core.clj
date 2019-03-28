(ns life-fhir-gen.core
  (:require
    [cheshire.core :as json]
    [clojure.tools.cli :as cli])
  (:import
    [java.time OffsetDateTime Instant ZonedDateTime ZoneId LocalDate]
    [java.util UUID])
  (:gen-class))


(defn- rand-local-date [^long start-year ^long end-year]
  (let [start (.toEpochDay (LocalDate/of start-year 1 1))
        end (.toEpochDay (LocalDate/of end-year 1 1))]
    (LocalDate/ofEpochDay (+ start (rand-int (- end start))))))


(defn- rand-date-time [start-year end-year]
  (let [zone (ZoneId/of "Europe/Berlin")
        start (.toEpochSecond (ZonedDateTime/of start-year 1 1 0 0 0 0 zone))
        end (.toEpochSecond (ZonedDateTime/of end-year 1 1 0 0 0 0 zone))]
    (-> (+ start (rand-int (- end start)))
        (Instant/ofEpochSecond)
        (OffsetDateTime/ofInstant zone))))


(defn gen-patients-with-observations
  ([n]
   (gen-patients-with-observations 0 n))
  ([start n]
   {:resourceType "Bundle"
    :id (str (UUID/randomUUID))
    :type "transaction"
    :entry
    (into
      []
      (mapcat
        (fn [patient-index]
          (into
            [{:resource
              {:resourceType "Patient"
               :id (str patient-index)
               :gender (rand-nth ["male" "female"])
               :birthDate (str (rand-local-date 1950 2000))}
              :request
              {:method "POST"
               :url "Patient"}}]
            (for [encounter-index (range 10)
                  observation-index (range 10)]
              {:resource
               {:resourceType "Observation"
                :id (str patient-index "-" encounter-index "-" observation-index)
                :status "final"
                :subject {:reference (str "Patient/" patient-index)}
                :code
                {:coding
                 [{:system "test"
                   :code (str observation-index)}]}
                :valueQuantity
                {:value (rand)}
                :effectiveDateTime (str (rand-date-time 2000 2010))}
               :request
               {:method "POST"
                :url "Observation"}}))))
      (range start (+ start n)))}))


(def cli-options
  [["-s" "--start START" "Patient index to start."
    :parse-fn #(Long/parseLong %)]
   ["-n" "--num NUM" "Number of patients to generate."
    :parse-fn #(Long/parseLong %)]
   ["-v" "--version"]
   ["-h" "--help"]])


(defn print-version []
  (println "life-fhir-gen version 0.1")
  (System/exit 0))


(defn print-help [summary exit]
  (println "Usage: life-fhir-gen [-n num] [-s start -n num]")
  (println summary)
  (System/exit exit))


(defn -main [& args]
  (let [{{:keys [start num version help]} :options :keys [summary]}
        (cli/parse-opts args cli-options)]
    (when version
      (print-version))
    (when help
      (print-help summary 0))
    (cond
      num
      (-> (gen-patients-with-observations (or start 0) num)
          (json/generate-string {:pretty true})
          (println))
      :else
      (print-help summary 1)))
  )


(comment
  (str (rand-date-time 2000 2010))
  (gen-patients-with-observations 1)
  )

