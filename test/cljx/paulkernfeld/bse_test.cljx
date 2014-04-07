;; Copyright 2014 Paul Kernfeld. This file is part of bitcoin-script-explorer,
;; which is licensed under the GNU GPL v3. See LICENSE for details.

(ns paulkernfeld.bse-test
  #+cljs (:require-macros [cemerick.cljs.test :refer (deftest is are)])
  (:require [paulkernfeld.bse :as bse]
            #+cljs [cemerick.cljs.test :as t]
            #+clj [clojure.test :refer (deftest is are)])
  (:import [paulkernfeld.bse State Parsed Op]))

(deftest parse-int
  (is (= 0 (bse/parse-int "0" 10)))
  (is (= 10 (bse/parse-int "10" 10)))
  (is (= 16 (bse/parse-int "10" 16))))

(deftest to-hex-test
  (is (= "01" (bse/to-hex [1])))
  (is (= "0102" (bse/to-hex [1 2])))
  (is (= "01ff" (bse/to-hex [1 255]))))

(deftest from-hex-test
  (is (= [1] (bse/from-hex "01")))
  (is (= [1 2] (bse/from-hex "0102")))
  (is (= [128 255] (bse/from-hex "80ff"))))

(deftest op-true-test
  (is (= (bse/State. [[3] [2] [1]] "unfinished") (bse/op-true (bse/State. [[3] [2]] "unfinished")))))

(deftest op-dup-test
  (is (= (bse/State. [[1] [2] [2]] "unfinished") (bse/op-dup (bse/State. [[1] [2]] "unfinished")))))

(deftest op-equalverify-test
  (is (= (bse/State. [[1] [2] [0]] false) (bse/op-equalverify (bse/State. [[1] [2]] "unfinished"))))
  (is (= (bse/State. [[1] [1] [1]] true) (bse/op-equalverify (bse/State. [[1] [1]] "unfinished")))))

(deftest parse-test
  #+clj (is (thrown? Exception (bse/parse [0xff])))
  #+cljs (is (thrown? js/Error (bse/parse [0xff])))
  )

(deftest parse-full-test
  (is [] (bse/parse-full []))
  #+clj (is [(Op. 0x51 "OP_TRUE" bse/op-true)] (bse/parse [0x51]))

  #+clj (is (thrown? Exception (bse/parse-full [0x02])))
  #+cljs (is (thrown? js/Error (bse/parse-full [0x02])))

  #+clj (is (thrown? Exception (bse/parse-full [0xff])))
  #+cljs (is (thrown? js/Error (bse/parse-full [0xff])))
  ) ;; invalid opcode -> error

(deftest execute-full-test
  (is (= [(bse/State. [] "unfinished")]
         (bse/execute-full [])))
  (is (= [(bse/State. [] "unfinished") (bse/State. [] "unfinished")]
         (bse/execute-full [(Op. 0x00 "OP_MOCK" bse/noop)])))
  (is (= [(bse/State. [] "unfinished") (bse/State. [[1]] "unfinished")]
         (bse/execute-full [(Op. 0x00 "OP_MOCK" bse/op-true)])))
  (is (= [(bse/State. [] "unfinished") (bse/State. [[1]] "unfinished") (bse/State. [[1]] "unfinished")]
         (bse/execute-full [(Op. 0x00 "OP_MOCK" bse/op-true) (Op. 0x00 "OP_MOCK" bse/noop)]))))

;; maybe = doesn't work for js objects?
;;#+cljs (deftest execute-js-test
;;         (is (= (array (array) (array (array 1)) (array (array 1)))
;;                (bse/execute-js [(Op. "OP_MOCK" bse/op-true) (Op. "OP_MOCK" bse/noop)]))))
