;; Copyright 2014 Paul Kernfeld. This file is part of bitcoin-script-explorer,
;; which is licensed under the GNU GPL v3. See LICENSE for details.

(ns paulkernfeld.bse
  (:require
   paulkernfeld.bse.js.sha256
   [paulkernfeld.bse.sha256 :refer [sha256]]
   paulkernfeld.bse.js.ripemd160
   [paulkernfeld.bse.ripemd160 :refer [ripemd160]]))

; should use the like (enable-console-print!)
(defn println-patched [argz]
  (js/console.log argz))

(defn throw-patched [error]
  (throw (js/Error error)))

(defn parse-int [string radix]
  (js/parseInt string radix))

(defn byte-to-hex [abyte]
  (let [asstring (.toString abyte 16)]
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

(defrecord Op [opcode name execute description])

(defrecord Parsed [op op-size])

; stack is a seq of seqs of bytes
; result is "unfinished" "success" or "failure" 
(defrecord State [stack result])

(defn noop [state] state)

(defn make-op-push [data] (fn [state] (State. (conj (:stack state) data) (:result state))))

(defn make-op-push-description [data]
  (str
   "Push "
   (count data)
   " bytes onto the stack: "
   (apply
    str
    (map
     (fn [part] (str "<br>" (to-hex part)))
     (partition-all 8 data)))))

(defn op-true [state] (State. (conj (:stack state) [1]) (:result state)))

(defn op-return [state] (State. (:stack state) "failure"))

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
     (case (and top-2-equal (not (= (:result state) "failure")))
       false "failure"
       true "success"))))

(defn hash160 [input] (from-hex (ripemd160 (sha256 (to-hex input)))))

(defn op-hash160 [state] (State. (conj (:stack state) (hash160 (peek (:stack state)))) (:result state)))

(defn op-checksig [state] (State. (conj (:stack state) [1]) (:result state)))

(defn parse [script]
  (let [opcode (first script)
        take-opcode (take opcode (rest script))]
    (cond
     (and (>= opcode 1) (<= opcode 0x4c))
     (if
         (< (count (rest script)) opcode)
       (throw-patched (str "Not enough bytes to push for OP_PUSH_" opcode))
       (Parsed. (Op. opcode (str "OP_PUSH_" opcode) (make-op-push take-opcode) (make-op-push-description take-opcode)) (inc opcode)))
     (= opcode 0x51) (Parsed. (Op. 0x51 "OP_TRUE" op-true "Push the value 0x01 onto the stack") 1)
     (= opcode 0x6a) (Parsed. (Op. 0x6a "OP_RETURN" op-return "Mark the transaction as invalid") 1)
     (= opcode 0x76) (Parsed. (Op. 0x76 "OP_DUP" op-dup "Push the top item onto the stack") 1)
     (= opcode 0x88) (Parsed. (Op. 0x88 "OP_EQUALVERIFY" op-equalverify "Returns 1 if the top two items on the stack are exactly equal, 0 otherwise. Then, marks transaction as invalid if top stack value is not true.") 1)
     (= opcode 0xa9) (Parsed. (Op. 0xa9 "OP_HASH160" op-hash160 "Hash the top item on the stack, first with SHA256 then with RIPEMD160.") 1)
     (= opcode 0xac) (Parsed. (Op. 0xac "OP_CHECKSIG" op-checksig "The entire transaction's outputs, inputs, and script are hashed. Returns 1 if signature used by OP_CHECKSIG was a valid signature for this hash and public key. Otherwise, returns 0. NOTE: since this Bitcoin Script implementation does not actually include the whole transaction, this method is mocked out to return true.") 1)
     :else (throw-patched (str "Opcode " (to-hex [opcode]) " isn't supported yet. Feel free to submit a pull request, though!")))))

;; Summarize the part of the script that is being parsed, for readability
(defn summary [script] 
  (if (<= (count script) 12)
    ;; If it's short, show the whole thing
    (to-hex script)
    ;; If it's long, just show the start and end
    (str (to-hex (take 6 script)) "..." (to-hex (take-last 6 script)))))

(defn parse-full [script]
  (if (empty? script)
    []
    (try 
      (let [script-parsed (parse script)]
        (cons (:op script-parsed) (parse-full (drop (:op-size script-parsed) script))))
      (catch js/Object e
        (throw-patched (str e " at parse-full(" (summary script) ")<br>"))))))

(defn parse-js [parsed]
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
(defn js-stack [stack]
  (apply array (map (fn [frame] (to-hex frame)) stack)))

;; Convert each state object into a JS object, and put them into an array
(defn execute-js [parsed]
  (apply array (map 
                (fn [state]
                  (js-obj
                   "result" (:result state)
                   "stack" (js-stack (:stack state))))
                (execute-full parsed))))

(defn print-thing [x] (println-str x))

(def private "5JN69erJZLVRxeaPdvFykuZJpstqzhiKz6z3dA2bRWGSzDmNUz1")

(def public "1Hvstviy6JzGxNkHoxKuQQfagTodD24poF")
