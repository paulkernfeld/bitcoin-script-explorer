;; Copyright 2014 Paul Kernfeld. This file is part of bitcoin-script-explorer,
;; which is licensed under the GNU GPL v3. See LICENSE for details.

(ns paulkernfeld.bse)

#+clj (set! *warn-on-reflection* true)

; should use the like (enable-console-print!)
(defn println-patched [argz]
  #+clj (println argz)
  #+cljs (js/console.log argz))

(defn throw-patched [error]
  #+clj (throw (Exception. error))
  #+cljs (throw (js/Error error)))

(defn parse-int [string radix]
  #+clj (Integer/parseInt string radix)
  #+cljs (js/parseInt string radix))

(defn byte-to-hex [abyte]
  #+clj (format "%02x" abyte)
  #+cljs (let [asstring (.toString abyte 16)]
           (if (= 1 (count asstring))
             (str "0" asstring)
             asstring)))

; input: a seq of bytes
; output: lower-case hex string
(defn to-hex [bytes]
  (apply str 
   (map byte-to-hex bytes)))

; Todo: make JS-friendly
; input: hex string
; output: vector of bytes
(defn from-hex [hexes]
  (map
   (fn [hex] (parse-int (apply str hex) 16))
   (partition 2 hexes)))   

(defrecord Op [name execute])

(defrecord Parsed [op remaining])

; stack is a seq of seqs of bytes
; result is "unfinished," ...
(defrecord State [stack result])

(defn noop [state] state)

(defn make-op-push [data] (fn [state] (State. (conj (:stack state) data) (:result state))))

(defn op-true [state] (State. (conj (:stack state) [1]) (:result state)))

(defn op-dup [state] (State. (conj (:stack state) (peek (:stack state))) (:result state)))

; This should only be true if the previous result wasn't false
(defn op-equalverify [state] 
  (let [top-2-equal (= (first (take-last 1 (:stack state))) (first (take-last 2 (:stack state))))]
    (State. 
     (conj 
      (:stack state)
      (case top-2-equal
        false [0]
        true [1]))
     top-2-equal)))

; Mocked out to return a constant, because the real hash depends on the input (txin?)
(defn op-hash160 [state] (State. (conj (:stack state) (from-hex "0000111122223333444455556666777788889999"))  (:result state)))

(defn op-checksig [state] (State. (conj (:stack state) [1]) (:result state)))

(defn parse [script]
  (let [opcode (first script)]
    (cond
     (and (> opcode 1) (<= opcode 0x4c))
     (if
         (< (count (rest script)) opcode)
       (throw-patched "NOT ENOUGH BYTES TO PUSH")
       (Parsed. (Op. (str "OP_PUSH_" opcode "_" (to-hex (take opcode(rest script)))) (make-op-push (take opcode(rest script)))) (drop opcode (rest script))))
     (= opcode 0x51) (Parsed. (Op. "OP_TRUE" op-true) (rest script))
     (= opcode 0x76) (Parsed. (Op. "OP_DUP" op-dup) (rest script))
     (= opcode 0x88) (Parsed. (Op. "OP_EQUALVERIFY" op-equalverify) (rest script))
     (= opcode 0xa9) (Parsed. (Op. "OP_HASH160" op-hash160) (rest script))
     (= opcode 0xac) (Parsed. (Op. "OP_CHECKSIG" op-checksig) (rest script))
     :else (throw-patched (str "UNSUPPORTED OPCODE: " (to-hex [opcode]))))))

(defn parse-full [script]
  (if (empty? script)
    []
    (cons (:op (parse script)) (parse-full (:remaining (parse script))))))

#+cljs (defn parse-js [parsed]
         (apply array parsed))

;; returns all states after this set of ops is executed on this start state
;; includes the start state
(defn execute [ops state]
  (if (empty? ops)
    [state]
    (cons state (execute (rest ops) (let [x ((:execute (first ops)) state)] (do (println-patched x) x))))))

(defn execute-full [ops]
  (execute ops (State. [] "unfinished")))

;; Converts the stack object into JS format, w/ arrays
#+cljs (defn js-stack [stack]
        (apply array (map (fn [frame] (to-hex frame)) stack)))

;; Convert each state object into a JS object, and put them into an array
#+cljs (defn execute-js [parsed]
         (apply array (map 
                       (fn [state]
                         (js-obj
                          "result" (:result state)
                          "stack" (js-stack (:stack state))))
                       (execute-full parsed))))

(defn print-thing [x] (println-str x))

(def private "5JN69erJZLVRxeaPdvFykuZJpstqzhiKz6z3dA2bRWGSzDmNUz1")

(def public "1Hvstviy6JzGxNkHoxKuQQfagTodD24poF")

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

(def a-parsed (parse-full (from-hex "483045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a58012102c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2f")))
(println-patched (map :name a-parsed))
(println-patched (execute-full a-parsed))

(def b-parsed (parse-full (from-hex "76a914000011112222333344445555666677778888999988ac")))
(println-patched (map :name b-parsed))
(println-patched (execute-full b-parsed))

(def all-parsed (parse-full (from-hex "483045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a58012102c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2f76a914000011112222333344445555666677778888999988ac")))
(println-patched (map :name all-parsed))
(println-patched (execute-full all-parsed))

#+cljs (println-patched (execute-js all-parsed))
