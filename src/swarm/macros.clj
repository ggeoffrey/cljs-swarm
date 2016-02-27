(ns swarm.macros
  "Where some witchcraft takes place. If you dare to enter this misterious place
  be sure to know the secret art of Abstract Syntax Tree manipulation."
  (:require [clojure.string :as s]))



(defmacro ref-to-native
  "Give a referenc to a native function
  while preserving the original binding"
  [native-object function]
  `(-> ~native-object
       (aget ~function)
       (.bind ~native-object)))



(defmacro constraint
  "Generate a condition list that will keep a boid in bounds for a particular axis"
  [position & {:keys [on max min]}]
  (let [setter (symbol (str ".set"
                            (s/upper-case (str on))))
        accessor (symbol (str ".-" on))]
    `(let [min# (cond
                (= :opposite ~min) (- ~max)
                :else ~min)]
         (cond
          (< (~accessor ~position) min#) (~setter ~position ~max)
          (> (~accessor ~position) ~max) (~setter ~position min#)))))
