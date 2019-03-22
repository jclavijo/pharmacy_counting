(ns pharmacy-counting.core
  (:gen-class))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;(def acct-map {})

;;boot
(defn -main
  [& args]
  (if-not (empty? args)
    ;(try
      (with-open [rdr (clojure.java.io/reader (last args))]
        (binding [*in* rdr] 
          (loop [line-input (do (read-line))
                 acct-map []]
            (if (nil? line-input)
              (println "loop done");
              (do
                (println "-M:" line-input acct-map);;)
                (recur (read-line)
                       (conj acct-map line-input)
                       )
                ))
            ))
        )
      ;; (catch Exception e (str "Error: File input type error or file not found. Check path and spelling.\n" 
      ;;                         (last args) "\n"
      ;;                         e)));try
    (println "How to use this command: Enter the name of the text file input to process."
             "'./input/input.txt'")))


