(ns fwpd.core)
(def filename "suspects.csv")

(def vamp-keys [:name :glitter-index])

(defn str->int
  [str]
  (Integer. str))

(def conversions {:name identity
                  :glitter-index str->int})

(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))

(defn parse
  "convert a csv into rows of columsn"
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\n")))

(defn mapify
  "return a seq of maps like {:name \"eddie cull\" :glitter-index 0}"
  [rows]
  (map (fn [unmapped-row]
         (reduce (fn [row-map [vamp-key value]]
                   (assoc row-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys unmapped-row)))
       rows))

(def suspects (mapify (parse (slurp filename))))

(defn glitter-filter
  [minimum-glitter records]
  (filter #(>= (:glitter-index %) minimum-glitter) records))


(def vampire-names (map :name (glitter-filter 3 suspects)))

(defn append-suspect
  "add a suspect to the end of the suspects list"
  [suspect]
  (into suspects suspect))

;; Chapter 5 Functional Programming
;; We have so far been using pure functions (except println and rand)
;; We know these are pure because they have "Referential Transparency" meaning they will always return the same outputs for each input and
;; they don't have any side effects. Since they are so well isolated it's easier to rationalize about our program
;; Clojure makes this easy for us as it's nearly impossible to change the underlying core data structures in clj. They're all immutable

;; However this requires a change in how we think about what we are doing.
;; If we ignore map and reduce the functional counterpart to conventional "for / if" loops is recursion!
(defn sum
  ;; if no total is given (first run) start with 0 as accumulating total
  ([vals] (sum vals 0))
  ;; if total is not 0
  ([vals accumulating-total]
   ;; if out of values to accumulate return accumulating total
   (if (empty? vals)
     accumulating-total
     ;; Else call sum again with the remainder of the vals and the first value added to the total
     (sum (rest vals) (+ (first vals) accumulating-total)))))

;; Lets walk through the results of each sum call
;; first
(sum [39 5 1])
(sum [39 5 1] 0)
(sum [5 1] 39)
(sum [1] 44)
(sum [] 45) ;-> 45

;; For performance reasons we actually want to use recur here instead
;; as clojure is not optimized for tail call recursion (erlang and elixir are)
(defn better-sum
  ([vals] (sum vals 0))
  ([vals acc-total]
   (if (empty? vals)
     acc-total
     (recur (rest vals) (+ (first vals) acc-total)))))
(better-sum [39 5 1])

;; # Cool things to do with Pure functions
;; `comp`
;; compose inc and multiply (multiply the args and then inc the result)
((comp inc *) 2 3)
;; It's important to note that the first function applied (*) can take any number of args, but every other function can only take 1
;; sooooo ((comp * inc) 2 3) won't work for arity reasons

(def character
  {:name "Funky BOI"
   :attributes {:int 10
                :str 4
                :dex 5}})
(def c-int (comp :int :attributes))
(def c-str (comp :str :attributes))
(def c-dex (comp :dex :attributes))
(c-int character)
;; We could have just written an anonymous function but that's a lot more work and less pretty
(fn [char] (:str (:attributes char)))

;; So what do you do if you need to have compose take more than one arg? You wrap it in an anonymous fn
(defn spell-slots
  [char]
  ;; # of spells is half int plus 1 rounded down (int)
  (int (inc (/ (c-int char) 2))))
(spell-slots character)
;; here's how we could do it with compose, isn't that nice?
(def spell-slots-comp (comp int inc #(/ % 2) c-int))
(spell-slots-comp character)

;; Here's joining any two arbitrary functions together
(defn two-comp
  [f g]
  (fn [& args]
    (f (apply g args))))

((two-comp inc *) 5 2)

;; `memoize`

;; non-memoized example
(defn sleepy-identity
  "returns the given arg after 1 second"
  [x]
  (Thread/sleep 1000)
  x)
;; Everytime we call it it will take one second so instead lets cache the result
(def memo-sleepy-identity (memoize sleepy-identity))
;; This will only take one second the first time (memo-sleepy-identity "Mr. Captain")
;; this is especially useful for intensive computation heavy fn's or network requests

;; PEG THING
