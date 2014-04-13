(ns paulkernfeld.bse.ripemd160
  (:require
   paulkernfeld.bse.js.ripemd160))

(defn ^:extern ripemd160
  [s]
  (.toString (.RIPEMD160 js/CryptoJS (.parse (.-Hex (.-enc js/CryptoJS)) s))))
