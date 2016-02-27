(ns swarm.macros
  "Where some witchcraft takes place. If you dare to enter this misterious place
  be sure to know the secret art of Abstract Syntax Tree manipulation."
  (:require [clojure.string :as s]))


(defmacro constrain
  "Generate a conditions list that will keep a boid in bounds for a particular axis."
  [position & {:keys [on max min]}]
  (let [;; Generate the setter name
        setter (symbol (str ".set"
                            (s/upper-case (str on))))
        ;; generate the field accessor
        accessor (symbol (str ".-" on))]
    `(let [min# (cond
                 (= :opposite ~min) (- ~max)
                 :else ~min)]
       (cond
        ;; if N is lower than the limit, set it to max
        (< (~accessor ~position) min#) (~setter ~position ~max)
        ;; if N is higher than the limit, set it to min
        (> (~accessor ~position) ~max) (~setter ~position min#)))))
