(ns paulkernfeld.bse.ripemd160-test
  (:require
   paulkernfeld.bse.js.ripemd160
   [cemerick.cljs.test :as t]
   [paulkernfeld.bse.ripemd160 :refer [ripemd160]])
  (:require-macros
   [cemerick.cljs.test :refer [is deftest with-test run-tests testing]]))

(deftest ripemd160-test
  (is (= "98e0b13e77bc80cdf9dce5c90f007ce24229c800" (ripemd160 "kittens"))))
