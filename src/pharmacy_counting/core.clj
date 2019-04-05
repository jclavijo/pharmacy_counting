(ns pharmacy-counting.core
  (:gen-class)
  (:require [clojure.core.reducers :as r]
            [clojure.string :as str]
            [iota :as iota]))

(def column-names [:id :prescriber_last_name :prescriber_first_name :drug_name :drug_cost])
(defn parse-colum-names
  "returns a list of keys from the supplied string and delimiter"
  [line reg-exp]
  (map keyword (str/split line reg-exp)))

(defn form-label-map
  "Returns a map with the values in string, split by regext delimiter and mapped to keys :drug_name {:total_drug_cost, :prescriber_list #{}, :prescriber_count}"
  [column-label line-str reg-exp]
  ;; {:pre [(= (count (str/split line-str reg-exp))
  ;;           (count column-label))]}
  (try
    (let [line-str-list (str/split line-str reg-exp)
          cost-idx 4
          line-list (update line-str-list cost-idx #(Float/parseFloat %))]
      (zipmap column-label line-list))
    (catch Exception e (str "Error:" e))))

(defn sorted-by-drug-cost-then-name
  "returns a map that has been sorted in decending order by :total_drug_cost and then by it's key :drug_name"
  [results-map]
  (into (sorted-map-by #(compare [(get-in results-map [%2 :total_drug_cost]) %1]
                                 [(get-in results-map [%1 :total_drug_cost]) %2]))
        results-map))

(defn reduce-to-drug-groups
  "Reducer function, used in parallel to reduce and transform the results of a map supplied with keys {:drug_name drug_cost :prescriber_last_name :prescriber_first_name} to transformation of a map with :drug_name {:total_drug_cost, :prescriber_list #{}, :prescriber_count}"
  ([] {}) ;; Necessary base case for use as combiner in fold
  ([m {:keys [drug_name drug_cost prescriber_last_name prescriber_first_name]}]
   (try
     (let [m-total-drug-cost (if-some [a (get-in m [(keyword drug_name) :total_drug_cost])]
                               a 0)
           m-prescriber-list (if-some [a (get-in m [(keyword drug_name) :prescriber_list])]
                               a #{});make transient
           new-total-drug-cost (+ m-total-drug-cost (if-some [a drug_cost] drug_cost 0))
           new-prescriber-list (conj m-prescriber-list (str prescriber_first_name prescriber_last_name))
           num-prescriber (count new-prescriber-list)]
       (assoc m (keyword drug_name)
              (hash-map :total_drug_cost new-total-drug-cost
                        :prescriber_list new-prescriber-list
                        :num_prescriber  num-prescriber)))
     (catch Exception e (str "Error:reduce-to-drug-groups:" e (.getMessage e))))))

(defn merge-drug-cost
  "Combinef function that combines/unions two maps with :drug_name {:total_drug_cost, :prescriber_list #{}, :prescriber_count}"
  ([] {})
  ([& ms] (apply merge-with
                 (fn drug-values
                   [v1 v2]
                   (let [{v1-total-drug-cost :total_drug_cost
                          v1-prescriber-list :prescriber_list
                          v1-num-prescriber  :num_prescriber} v1
                         {v2-total-drug-cost :total_drug_cost
                          v2-prescriber-list :prescriber_list
                          v2-num-prescriber  :num_prescriber} v2
                         total-drug-cost (+ v1-total-drug-cost v2-total-drug-cost)
                         prescriber-list (clojure.set/union v1-prescriber-list v2-prescriber-list)
                         num-prescriber (count prescriber-list)]
                     (hash-map :total_drug_cost total-drug-cost
                               :prescriber_list prescriber-list
                               :num_prescriber  num-prescriber)))
                 ms)))

(defn print-drug-names
  "writes the output file for map"
  [drug-map output-file]
  (with-open [wtr (clojure.java.io/writer output-file)]
    (binding [*out* wtr]
      (print "drug_name,num_prescriber,total_cost")
      (apply println
             (filter some? 
                     (map (fn my-print [[k {:keys [total_drug_cost num_prescriber]}]]
                            (when (and k total_drug_cost num_prescriber)
                                        ;(println
                              (str "\n" (name k) ","  num_prescriber "," (format "%.2f" total_drug_cost))))
                                        ;)
                          drug-map))))))


(defn parse-and-map
  "Returns a map of the drugs with the number of prescribers and total cost that has been sorted by drug-cost and name"
  [iota-seq-file column-labels]
   (->> iota-seq-file
        (next)
        (r/filter identity)
        (r/map #(form-label-map column-labels % #","))
        (r/fold
         1024
         merge-drug-cost
         reduce-to-drug-groups)
        (sorted-by-drug-cost-then-name)))

;;boot
(defn -main
  [& args]
  (if-not (empty? args)
    (try
      (let [input-file (first args)
            iota-file (iota/seq input-file)
            output-file (second args)]
        (-> iota-file
            (parse-and-map column-names)
            (print-drug-names output-file)))
      (catch Exception e (str "Error: File error, input file or output file not found. Check path and spelling.  " 
                              (last args)
                              )));try
    (println "How to use this command: Enter two arguments the name of the input text file and the output text file in order to process the transform the results.\n"
             "'./input/input.txt' './output/output.txt'")))

;; If your input data, **`itcont.txt`**, is
;; ```
;; id,prescriber_last_name,prescriber_first_name,drug_name,drug_cost
;; 1000000001,Smith,James,AMBIEN,100
;; 1000000002,Garcia,Maria,AMBIEN,200
;; 1000000003,Johnson,James,CHLORPROMAZINE,1000
;; 1000000004,Rodriguez,Maria,CHLORPROMAZINE,2000
;; 1000000005,Smith,David,BENZTROPINE MESYLATE,1500
;; ```

;; then your output file, **`top_cost_drug.txt`**, would contain the following lines

;; ``` You are asked to generate a list of all drugs, the total number of UNIQUE individuals who prescribed the medication, and the total drug cost, which must be listed in descending order based on the total drug cost and if there is a tie, drug name in ascending order.

;; drug_name,num_prescriber,total_cost
;; CHLORPROMAZINE,2,3000
;; BENZTROPINE MESYLATE,1,1500
;; AMBIEN,2,300
;; ```
