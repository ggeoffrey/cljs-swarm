(ns swarm.macros)



(defmacro def- [item value]
  `(def ^:private ~item ~value))


(defmacro defonce-
  [item value]
  `(defonce ^:private ~item ~value))
