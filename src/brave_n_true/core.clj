(ns brave-n-true.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn too-enthusiastic
  "Return an enthusiastic greeting"
  [name]
  (str "OMG HI HOW ARE YOU " name " ITS BEEN WAY TOO LONG AND I HAVEN'T SEEN YOU"))

; We can use multi arity functions that take an variable number of args with the "&"
; - this lets us define up to infinite number of args for our input
(defn codger-communication
  [whippersnapper]
  (str "Get off my lawn, " whippersnapper "!!!"))

(defn codger
  [& whippersnappers]
  (map codger-communication whippersnappers))

(codger "Billy" "Anne-Marie" "The Incredible Bulk")

; Destructuring
; it works on vectors
(defn only-the-first-value
  [[first-thing]]
  first-thing)
(only-the-first-value ["ahht" "cat" "bat"])

; you can use it multiple times
(defn first-three
  [[first-thing second-thing third-thing & the-rest]]
  (println (str "First is the best" first-thing))
  (println (str "Second is like good" second-thing))
  (println (str "Third is fine" third-thing))
  (println (str "The rest are dumb, here they are" (clojure.string/join "," the-rest))))

(first-three ["team" "numbers" "stuff" "poo" "yuck"])

; it works intuitively on maps
; you can specify the value in the args and its corresponding key in the map
; but this could be better
(defn treasure-coords
  [{lat :lat lng :lng}]
  (str "the coordinates are lat: " lat " long: " lng))

(treasure-coords {:lat 1 :lng 32})

; above we basically are just saying we want the keys in the map to correspond to the values
; since they match we can use the keys function
(defn treasure-coords-2
  [{:keys [lat lng]}]
  (str "the coordinates - 2 are lat: " lat " long: " lng))

(treasure-coords-2 {:lat 3 :lng 32})

; but maybe we want to hang on to that map after also destructuring (we can, it's just data)
(defn treasure-coords-3
  "create a string, returning the original coords"
  [{:keys [lat lng] :as treasure-location}]
  (str "the coordinates - 2 are lat: " lat " long: " lng)
  treasure-location)

(treasure-coords-3 {:lat 4 :lng 32})

; Anonymous Functions are a thing and look just like defined functions
(map (fn [name] (str "HI " name)) ["ben" "ricardo"])
; which is the same as
(def hi-namer (fn [name] (str "HI " name)))
(map hi-namer ["fred" "ches"])
; There's also a more compact way to make an anonymouse function usint the "#" sign and "*"
; "*" takes the place of a named arg and # takes the places of the "fn"
(#(* % 3) 8)
; this syntax allows us to pass in the funtion as an argument to another function
(map #(str "hi, " %)
     ["Ralphy" "Georgie"])
; You can also give multiple arguments to it using %position
(#(str %1 " and " %2) "bread" "butter")

;You can also return a function as a closure (meaning it has access to all the variables in the scope)
(defn inc-maker
  "Create a custom incrementor"
  [inc-by]
  #(+ % inc-by))

(def inc3 (inc-maker 3))
(inc3 7)

; THE SHIRES NEXT TOP MODEL
; a vector of maps, each containing the name and size of the bodypart
(def asym-hobbit-body-parts [{:name "head" :size 3}
                             {:name "left-eye" :size 1}
                             {:name "left-ear" :size 1}
                             {:name "mouth" :size 1}
                             {:name "nose" :size 1}
                             {:name "neck" :size 2}
                             {:name "left-shoulder" :size 3}
                             {:name "left-upper-arm" :size 3}
                             {:name "chest" :size 10}
                             {:name "back" :size 10}
                             {:name "left-forearm" :szie 3}
                             {:name "abdomen" :size 6}
                             {:name "left-kidney" :size 1}
                             {:name "left-hand" :size 2}
                             {:name "left-knee" :size 2}
                             {:name "left-thigh" :size 4}
                             {:name "left-lower-leg", :szie 3}
                             {:name "left-achilles" :size 1}
                             {:name "left-foot" :size 2}])

(defn matching-part
  "Return the corresponding right side part"
  [part]
  ; This #"^blah" syntax is a regex
  {:name (clojure.string/replace (:name part) #"^left-" "right-")
   :size (:size part)})

(defn symmetrize-body-parts
  "Take a seq of maps with :name and :size"
  [asym-body-parts]
  ; `loop` allows us to use recursion
  (loop [remaining-asym-parts asym-body-parts
         final-body-parts []]
    ; Exist early if there's no remaining parts
    (if (empty? remaining-asym-parts)
      final-body-parts
      ; This let does a couple of things,
      ; 1) create a new scope
      ; 2) inside the scope destructure the first elem of `remaining-asym-parts` into parts and the rest into the remaining
      ; 3) let forms allow us to eval the expression only once and re-use the result, (it's memoizing it)
      (let [[part & remaining] remaining-asym-parts]
        (recur remaining
               ; Use the `set` function to create a set consisting of part and matching part
               ; then use the `into` fn to add the set values to final-body-parts
               (into final-body-parts
                     ;we need to use a set because parts like 'chest and back' will have
                     ;identical `matching-part` results
                     (set [part (matching-part part)])))))))

; `loop` example
; nice compact loop
(loop [iteration 0]
  (println (str "Iteration " iteration))
  (if (> iteration 3)
    (println "Goodbye!")
    (recur (inc iteration))))
; It's possible to do the same thing with a normal function
; but it's very verbose, and loop is much more optimized for perf
(defn recursive-printer
  ; empty arity
  ([]
   (recursive-printer 0))
  ; 1 arity
  ([iteration]
   (println (str "Iteration no: "iteration))
   (if (> iteration 3)
     (println "Bye")
     (recursive-printer (inc iteration)))))

(recursive-printer)

; PART 2 A better Symmetrize-er
; In part one we long-formed the process of "process each element in a sequence and build a result"A
; In this section we will use reduce to make it better
; here's a reduce example
(reduce + [1 2 3 4]) ;returns 10
; this is the same as
(+ (+ (+ 1 2) 3) 4)
; reduce can also take an optional first arg
(reduce + 10 [1 2 3 4]) ; returns 20

; lets reimplement the resymmetrizer
; first this is way nicer and more readable
(defn better-symmetrize-body-parts
  "Takes a seq of maps that have :name and :size"
  [asym-body-parts]
  (reduce (fn [final-body-parts part]
            (into final-body-parts (set [part (matching-part part)])))
          []
          asym-body-parts))
(better-symmetrize-body-parts asym-hobbit-body-parts)

; HOBBIT VIOLENCE
(defn hit
  [asym-body-parts]
  (let [sym-parts (better-symmetrize-body-parts asym-body-parts)
        body-part-size-sum (reduce + (map :size sym-parts))
        target (rand body-part-size-sum)]
    (loop [[part & remaining] sym-parts
            accumulated-size (:size part)]
       (if (> accumulated-size target)
         part
         (recur remaining (+ accumulated-size (:size (first remaining))))))))

; This isn't working RN due to a nil being returned but it's exactly like the
; book TODO figure this out in a second
;; (hit asym-hobbit-body-parts)


; Chapter Exercises
; No 3
(defn dec-maker
  [dec-by]
  #(- % dec-by))
(def dec9 (dec-maker 9))

;; Setmap
(defn set-map
  "takes a function and seq returning a set of function applied to the sequence"
  [function values]
  (set (map function values)))

(set-map inc [1 2 3])
(set-map inc [1 1 2 2])
(set-map str [1 1 2 2])


;; CHAPTER 4

;; This chapter is about understanding what it is that makes up the core functions of clojure
;; We will call it "Programming to Abstractions"

;; Clojure doesn't care about the underlying datastructures as long as it adheres to the
;; abstraction of `first` `rest` and `cons` it works as a `seq`
(defn titleize
  [topic]
  (str topic " for the Brave and True"))

;; These will all return a list of the elements with the titleized results
(map titleize ["Hamsters" "Ragnarok"])
(map titleize '("Empathy" "Decoration"))
(map titleize #{"Elbows" "Soap Carving"})
;; We have to call second here because the arg is a map
(map #(titleize (second %)) {:uncomfortable-thing "Winking"})

;; Abstractions through Indirection

;; Indirection -> is a generic term for the way a language allows one name to have multiple,
;;   datastructure specific but still related meaning s
;; Polymorphism -> is one way way clojure allows for indirection. Poly Fn's dispatch to different bodies based
;;   on argument type.

;; With Sequences Clojure provides a lightweight type conversion function which always returns a list ex:
(seq '(1 2 3))
(seq [1 2 3])
(seq #{1 2 3})
(seq {:name "Bill" :occupation "Dude"}) ;; => returns key value pairs as a sequence

;; We can reconstruct a map with into
(into {} (seq {:a 1 :b 2}))

;; If we can map something we can also always reduce, filter, distinct, group-by, etc on it

;; Maps can also take multiple collections
(map str ["a" "b"] ["A" "B"])
;; Which works like
(list (str "a" "A") (str "b" "B"))

;; This super power also allows us to pass a mapping function with multiple arity
(def person-ratings [8.1 2.6 3.4])
(def dog-ratings [10 10 10])
(defn unify-rating
  [human dog]
  {:human human
   :dog dog})
(map unify-rating person-ratings dog-ratings)

;; Since it's all just data we can pass in multiple functions
;; here we are applying each function to numbers
(def sum #(reduce + %))
(def avg #(/ (sum %) (count %)))
(defn stats
  [numbers]
  (map #(% numbers) [sum count avg]))

(stats [3 4 10])
;; Because :keywords work like functions we can get values from a map easily
(def identities
  [{:alias "Batman" :real "Bruce"}
   {:alias "Superman" :real "Kent"}])
(map :real identities)


;; NEXT UP - REDUCE
;; reduce processes each element in a seq to build a result
;; here are some non-obvious examples
(reduce (fn [new-map [key val]]
          (assoc new-map key (inc val)))
        {}
        {:max 30 :min 10})
;; This uses an empty map (arg2) and treats the args as a seq of vecs ([:max 30] [:min 10]) applying the anon fn as it goes
;; its as if it does this
(assoc (assoc {} :max (inc 30))
       :min (inc 10))

;; Another use of reduce is to filter out keys from a map based on value
(reduce (fn [new-map [key val]]
          (if (> val 5)
            (assoc new-map key val)
            new-map))
        {}
        {:human 4.1
         :dog 10}) ;; => {:dog 10}
;; The takeaway here is that reduce is the goto for getting a new value from a sequence

;; Some other cool guys
(take 3 [1 2 3 4 5 6 7]) ; => (1 2 3)
(drop 3 [1 2 3 4 5 6 7]) ; => (4 5 6 7)

;; These combine with while allow for a predicate function to know when to stop
(def food-journal
  [{:month 1 :day 1 :human 5.3 :dog 2.3}
   {:month 1 :day 2 :human 5.2 :dog 2.0}
   {:month 2 :day 1 :human 5.1 :dog 2.1}
   {:month 2 :day 1 :human 5.0 :dog 2.5}
   {:month 3 :day 2 :human 4.9 :dog 3.3}
   {:month 3 :day 4 :human 4.6 :dog 3.0}
   {:month 4 :day 1 :human 5.3 :dog 3.3}
   {:month 4 :day 2 :human 5.2 :dog 3.4}
   {:month 5 :day 1 :human 5.3 :dog 3.5}])

;; These will step one at a time through the seq (so it must be sorted)
;; get everything from jan to feb
(take-while #(< (:month %) 3) food-journal)
;; get the rest
(drop-while #(< (:month %) 3) food-journal)
;; get data for only feb and march
(take-while #(< (:month %) 4)
            (drop-while #(< (:month %) 2) food-journal))

;; filter (processes each element)
(filter #(< (:human %) 5) food-journal)
;; Some returns the first thruthy value (not false or nil) from predicate function
(some #(> (:dog %) 3) food-journal) ; => true
(some #(and (> (:dog %) 3) %) food-journal) ; =>  {...}

(sort [3 1 2])
(sort-by count ["aaa" "c" "bb"])

(concat [1 2] [3 4])

;; Lazy Seq
;; Clojure map and other functions of the like calls seq on the collection passed to it
;; BUT many fn's return a lazy seq so the values are only computed once when you try to access them
(def vampire-database
  {0 {:makes-blood-puns? false :has-pulse? true :name "McFishwich"}
    1 {:makes-blood-puns? false :has-pulse? true :name "McMakson"}
    2 {:makes-blood-puns? true :has-pulse? false :name "Damon Salvatore"}
    3 {:makes-blood-puns? true :has-pulse? true :name "Mickey Mouse"}})

(defn vampire-related-details
  "Gets the vampire by security number"
  [social-security-number]
  ;(Thread/sleep 1000)
  (get vampire-database social-security-number))

(defn vampire?
  [record]
  (and (:makes-blood-puns? record)
       (not (:has-pulse? record))
       record))

(defn identify-vampire
  [social-security-numbers]
  (first (filter vampire?
                 (map vampire-related-details social-security-numbers))))

(time (vampire-related-details 0)) ;; -> returns "Elapsed time: 1001.042 msecs" and then the db record
;; If we have a million records it would take 1000 msec * 10^6 to find each vampire
;; Maps are lazy however so we won't get any values until we try and access the mapped element
;; EX
(time (def mapped-details (map vampire-related-details (range 0 100000)))) ;; -> Elapsed time 0.049 msecs
;; The above returns a function without evaluating all the potential mapped-details and since it's never used it takes practically no time

;; Now when we access it we will incur the whole cost of the lookup
(time (first mapped-details)) ;; -> Elapsed time: 32030.767 msecs
;; this only took 32 seconds, but it's weird we should have only taken 1 secons accessing one element
;; the time is not 1 million seconds tho. The reason for this is that Clojure "chunks" it's lazy sequences
;; so it prepared the first 32 values for this evaluation
;; however calling it again doesn't incur the same cost as the seq has already been realized
(time (first mapped-details)) ;; -> Elapsed time: 0.022 msecs

;; now we know that we can efficiently mine the campire database to find the culprit
(time (identify-vampire (range 0 1000000))) ;;-> Elapsed time: 32019.912 msecs
;; That's way better than a million

;; Another cool thing about clojure sequences is that they can be infinite!
(concat (take 8 (repeat "na")) ["Batman!"])
;; repeat here uses a non-terminating sequence
;; we can also use the repeatedly fn to call a function each time
(take 3 (repeatedly (fn [] (rand-int 100))))

(defn even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))

(take 10 (even-numbers))

;; THE COLLECTION ABSTRACTION
;; It's very similar to the seq abstraction
(empty? [])
(empty? ["no"])
;; one of the most important function is "into"
;; it will return the original data structure instead of just returning a seq
(map identity {:sunlight-reaction "Glitter!"})

(into {} (map identity {:sunlight-reation "Glitter"}))
;; the first collection doesn't have to be empty
(into ["cherry"] '("pine" "maple")) ;; => ["cherry "pine" "maple"]

(into {:tiny-boy "tim"} {:tiniest-tot "taters"
                         :meatiest-tiny "thicccc"})
;; -> {:tiny-boy "tim", :tiniest-tot "taters", :meatiest-tiny "thicccc"}
;; Conj is a similar function but does things in a different way
(conj [0] [1]) ;; => [0 [1]]
(conj [0] 1) ;; => [0 1]
(conj {:time "midnight"} [:place "spooky town"]) ;; => {:time "midnight", :place "spooky town"}

;; you could define conj in terms of into if you wanted
(defn my-conj
  [target & additions]
  (into target additions))

(my-conj [0] 1 2 3) ;;=> [0 1 2 3]

;; In Clojure this pattern isnt uncommon
;; often a function with do the same thing only one will take a rest param (conj) and the other
;; will take a sequelable data struction (into)


;; FUNCTION FUNCTIONS
;; Clojure does a lot and lets you take functions as an arguement and returns functions this is cool
;; Lets talk about apply and partial two examples of how cool this can be

;; apply
;; apply explodes a sequeable data structure so it can be passed to a function that expects a rest parameter
(max 0 1 2); => 2
;; but what if you wanted to find the greatest val in a vector
(max [0 1 2]) ; => [0 1 2]
;; bummer
;; lets try with apply
(apply max [0 1 2]); => 2

;; it's possible touse apply and conj to create an into
(defn my-into
  [target additions]
  (apply conj target additions))
(my-into [0] [1 2 3]) ; => [0 1 2 3]

;; partial takes a function and any number of args then returns a new fn
;; When you return the fn it calls the original with the og args you supplied and NEW args
(def add10 (partial + 10))
(add10 3) ; => 13

(def add-missing-elements
  (partial conj ["water" "earth" "air"]))
(add-missing-elements "fire" "glue")

;; we can define it
(defn my-partial
  [partialized-fn & args]
  (fn [& more-args]
    (apply partialized-fn (into args more-args))))
(def add20 (my-partial + 20))
(add20 3); => 23

;; in general we want to use partials when we are repeating the same combination of fns and args in different contexts (you could imagine checking a users permissions this way)
;; here's a example logger toy
(defn lousy-logger
  [log-level message]
  (condp = log-level
    :warn (clojure.string/lower-case message)
    :emergency (clojure.string/upper-case message)))

(def warn (partial lousy-logger :warn))
(warn "Red light ahead") ; "red light ahead"
;; calling warn here is the same as calling (lousy-logger :warn "Red light ahead")

;; Complement
;; early we defined an identify-vampire function to find one vampire amid a million people
;; what if we wanted to create a function to find all humans
(defn identify-humans
  [social-security-numbers]
  (filter #(not (vampire? %))
          (map vampire-related-details social-security-numbers)))

;; the section #(not (vampire? %)) is so common there's a complement fn for it
(def not-vampire? (complement vampire?))
(defn identify-humans2
  [social-security-numbers]
  (filter not-vampire?
          (map vampire-related-details social-security-numbers)))

(identify-humans2 [0 1 2 3])

;; here's what complement looks like
(defn my-complement
  [fun]
  (fn [& args]
    (not (apply fun args))))
(def my-pos? (complement neg?))
(my-pos? 1) ; -> true
(my-pos? -1) ; -> false
