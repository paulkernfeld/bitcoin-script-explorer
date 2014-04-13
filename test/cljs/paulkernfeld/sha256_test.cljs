(ns paulkernfeld.bse.sha256-test
  (:require
   paulkernfeld.bse.js.sha256
   [cemerick.cljs.test :as t]
   [paulkernfeld.bse.sha256 :refer [sha256]])
  (:require-macros
   [cemerick.cljs.test :refer [is deftest with-test run-tests testing]]))

(deftest sha256-test
  ;; "kittens" as hex
  (is (= "c81a7b1e755bdf87160ff008f94c8ecc21bc2a710a23bf5e1351300edc0231a1" (sha256 "6b697474656e73"))))
