(ns pharmacy-counting.core-test
  (:require [clojure.test :refer :all]
            [pharmacy-counting.core :refer :all]))

(def t-label-str "id,prescriber_last_name,prescriber_first_name,drug_name,drug_cost")
(def t-line1-str "1000000001,Smith,James,AMBIEN,100")
(def t-par1 '("1000000001,Smith,James,AMBIEN,100" "1000000002,Garcia,Maria,AMBIEN,200"))
(def t-line1-vec ["1000000001" "Smith" "James" "AMBIEN" "100"])
(def t-column-names [:id :prescriber_last_name :prescriber_first_name :drug_name :drug_cost])

(deftest test-form-label-map
  (let [t-column-names [:id :prescriber_last_name :prescriber_first_name :drug_name :drug_cost]
        t-line1-str "1000000001,Smith,James,AMBIEN,100"
        t-line1-str2 "1000000001,Smith,James,AMBIEN X,100"]
    (testing "forming a map from label and lines"
      (is (= {:id "1000000001",
              :prescriber_last_name "Smith",
              :prescriber_first_name "James",
              :drug_name "AMBIEN",
              :drug_cost 100.0}
             (form-label-map t-column-names t-line1-str #",")))
      (is (= {:id "1000000001",
              :prescriber_last_name "Smith",
              :prescriber_first_name "James",
              :drug_name "AMBIEN X",
              :drug_cost 100.0}
             (form-label-map t-column-names t-line1-str2 #","))))))


(deftest test-print-drug-names
  (let [drug-map {:CHLORPROMAZINE {:num_prescriber 2,
                                   :prescriber_list #{"JamesJohnson" "MariaRodriguez"},
                                   :total_drug_cost 3000.0},
                  (keyword "BENZTROPINE MESYLATE") {:num_prescriber 1,
                                                    :prescriber_list #{"DavidSmith"},
                                                    :total_drug_cost 1500.0},
                  :AMBIEN {:num_prescriber 2,
                           :prescriber_list #{"MariaGarcia" "JamesSmith"},
                           :total_drug_cost 300.0}}
        file-name "./insight_testsuite/tests/test_1/output/top_cost.txt"
        top-cost-string "drug_name,num_prescriber,total_cost\nCHLORPROMAZINE,2,3000 \nBENZTROPINE MESYLATE,1,1500 \nAMBIEN,2,300\n"]
    (print-drug-names drug-map file-name)
    (is (= "drug_name,num_prescriber,total_cost\nCHLORPROMAZINE,2,3000.00 \nBENZTROPINE MESYLATE,1,1500.00 \nAMBIEN,2,300.00\n"
           (slurp file-name)))
    ))

(deftest test-main
  (let [input-file  "./insight_testsuite/tests/test_1/input/itcont.txt"
        iota-file (iota/seq input-file)
        output-file  "./insight_testsuite/tests/test_1/output/top_cost_drug.txt"
        top-cost-string "drug_name,num_prescriber,total_cost\nCHLORPROMAZINE,2,3000.00 \nBENZTROPINE MESYLATE,1,1500.00 \nAMBIEN,2,300.00\n"]
    (-main input-file output-file)
    (is (= "drug_name,num_prescriber,total_cost\nCHLORPROMAZINE,2,3000.00 \nBENZTROPINE MESYLATE,1,1500.00 \nAMBIEN,2,300.00\n"
           (slurp output-file)))))
