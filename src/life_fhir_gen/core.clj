(ns life-fhir-gen.core
  (:require
    [cheshire.core :as json]
    [clojure.tools.cli :as cli])
  (:import
    [cern.jet.random.tdouble Normal]
    [java.math RoundingMode]
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


(defn rand-decimal
  "Returns a random decimal with a precision of 4 and a scale between 0 and 2.

  This should be typical for clinical decimal values."
  []
  (BigDecimal/valueOf (rand-int 10000) (rand-int 3)))


(defn sample-normal-decimal [mean sd scale]
  (-> (Normal/staticNextDouble mean sd)
      (BigDecimal/valueOf)
      (.setScale ^long scale RoundingMode/HALF_UP)))


(defn gen-body-weight-observation [patient-index encounter-index date value]
  {:resourceType "Observation"
   :id (str patient-index "-" encounter-index "-body-weight")
   :status "final"
   :subject {:reference (str "Patient/" patient-index)}
   :code
   {:coding
    [{:system "http://loinc.org"
      :code "29463-7"}]}
   :valueQuantity
   {:value value
    :unit "kg"}
   :effectiveDateTime (str date)})


(defn gen-body-height-observation [patient-index encounter-index date value]
  {:resourceType "Observation"
   :id (str patient-index "-" encounter-index "-body-height")
   :status "final"
   :subject {:reference (str "Patient/" patient-index)}
   :code
   {:coding
    [{:system "http://loinc.org"
      :code "8302-2"}]}
   :valueQuantity
   {:value value
    :unit "cm"}
   :effectiveDateTime (str date)})


(defn gen-bmi-observation [patient-index encounter-index date value]
  {:resourceType "Observation"
   :id (str patient-index "-" encounter-index "-bmi")
   :status "final"
   :subject {:reference (str "Patient/" patient-index)}
   :code
   {:coding
    [{:system "http://loinc.org"
      :code "39156-5"}]}
   :valueQuantity
   {:value value
    :unit "kg/m2"}
   :effectiveDateTime (str date)})


(defn body-weight [^BigDecimal body-height ^BigDecimal bmi]
  (.setScale
    ^BigDecimal (* bmi (* (/ body-height 100) (/ body-height 100)))
    1 RoundingMode/HALF_UP))


(defn gen-smoker-observation [patient-index encounter-index date]
  {:resourceType "Observation"
   :id (str patient-index "-" encounter-index "-smoker")
   :status "final"
   :subject {:reference (str "Patient/" patient-index)}
   :code
   {:coding
    [{:system "http://loinc.org"
      :code "72166-2"}]}
   :valueCodeableConcept
   {:coding
    [{:system "http://fhir.de/CodeSystem/raucherstatus"
      :code (rand-nth ["raucher" "exraucher" "nieraucher" "unbekannt"])}]}
   :effectiveDateTime (str date)})


(defn gen-patient [patient-index]
  (let [birth-date (rand-local-date 1950 2000)
        age (long (sample-normal-decimal 70 10 0))
        deceased-date (.plusYears birth-date age)]
    (cond->
      {:resourceType "Patient"
       :id (str patient-index)
       :gender (rand-nth ["male" "female"])
       :birthDate (str birth-date)}
      (.isBefore deceased-date (LocalDate/now))
      (assoc :deceasedDateTime (str deceased-date)))))


(defn rand-icd-10-code []
  (format "%s%02d" (char (+ 65 (rand-int 26))) (rand-int 100)))


(defn gen-condition [patient-index encounter-index date]
  {:resourceType "Condition"
   :id (str patient-index "-" encounter-index)
   :subject {:reference (str "Patient/" patient-index)}
   :code
   {:coding
    [{:system "http://hl7.org/fhir/sid/icd-10"
      :version "2016"
      :code (rand-icd-10-code)}]}
   :assertedDate (str date)})


(defn gen-specimen-liquid [patient-index encounter-index date type]
  {:resourceType "Specimen"
   :id (str patient-index "-" encounter-index "-" type)
   :subject {:reference (str "Patient/" patient-index)}
   :type
   {:coding
    []}
   :collection
   {:collectedDateTime (str date)}})


(defn bundle-entry [{:keys [resourceType id] :as resource}]
  {:resource
   resource
   :request
   {:method "POST"
    :url (str resourceType "/" id)}})


(defn gen-encounter [patient-index encounter-index]
  (let [date (rand-local-date 2000 2018)
        bmi (sample-normal-decimal 27.3 4.89 1)
        body-height (sample-normal-decimal 171 30.8 0)]
    [(gen-condition
       patient-index encounter-index date)
     (gen-body-weight-observation
       patient-index encounter-index date
       (body-weight body-height bmi))
     (gen-body-height-observation
       patient-index encounter-index date
       body-height)
     (gen-bmi-observation
       patient-index encounter-index date
       bmi)
     (gen-smoker-observation
       patient-index encounter-index date)]))


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
            [(bundle-entry (gen-patient patient-index))]
            (comp
              (mapcat #(gen-encounter patient-index %))
              (map bundle-entry))
            (range 2))))
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
  (def start 0)
  (def num 1)
  (gen-patients-with-observations 1)
  )

