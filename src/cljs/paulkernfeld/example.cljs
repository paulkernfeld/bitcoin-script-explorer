; Example scripts are taken from this transaction

; https://blockchain.info/rawtx/c0fdb774710943d8dbb5f823c00394452fb1a1d63a3a7a9fb8de41299b37f090?format=hex

; 0100000001a776d0ddaa671356fa167c3f1dfb73b82f40136c910724556b82735b942c14e1000000006b483045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a58012102c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2fffffffff0158387b03000000001976a914f5d214041d44860c8c08202a9e4263fc47a2fe8888ac00000000

; {
;     "locktime": 0,
;     "txid": "c0fdb774710943d8dbb5f823c00394452fb1a1d63a3a7a9fb8de41299b37f090",
;     "version": 1,
;     "vin": [
;         {
;             "scriptSig": {
;                 "asm": "3045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a5801 02c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2f",
;                "hex": "483045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a58012102c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2f"
;             },
;             "sequence": 4294967295,
;             "txid": "e1142c945b73826b552407916c13402fb873fb1d3f7c16fa561367aaddd076a7",
;             "vout": 0
;         }
;     ],
;     "vout": [
;         {
;             "n": 0,
;             "scriptPubKey": {
;                 "addresses": [
;                     "1PQnDVEi2u4e8rFmE3d9J51eQz8cQXpybC"
;                 ],
;                 "asm": "OP_DUP OP_HASH160 f5d214041d44860c8c08202a9e4263fc47a2fe88 OP_EQUALVERIFY OP_CHECKSIG",
;                 "hex": "76a914f5d214041d44860c8c08202a9e4263fc47a2fe8888ac",
;                 "reqSigs": 1,
;                 "type": "pubkeyhash"
;             },
;             "value": 0.58407
;         }
;     ]
; }

(ns paulkernfeld.bse.example
  (:require [paulkernfeld.bse :as bse]))

(def a-parsed (bse/parse-full (bse/from-hex "483045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a58012102c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2f")))
(bse/println-patched (map :name a-parsed))
(bse/println-patched (bse/execute-full a-parsed))

(def b-parsed (bse/parse-full (bse/from-hex "76a914000011112222333344445555666677778888999988ac")))
(bse/println-patched (map :name b-parsed))
(bse/println-patched (bse/execute-full b-parsed))

(def all-parsed (bse/parse-full (bse/from-hex "483045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a58012102c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2f76a914000011112222333344445555666677778888999988ac")))
(bse/println-patched (map :name all-parsed))
(bse/println-patched (bse/execute-full all-parsed))

(bse/println-patched (bse/execute-js all-parsed))
