(ns paulkernfeld.bse.sha256
  (:require
   paulkernfeld.bse.js.sha256))

(defn ^:extern sha256
  [s]
  (.toString (.SHA256 js/CryptoJS (.parse (.-Hex (.-enc js/CryptoJS)) s))))
