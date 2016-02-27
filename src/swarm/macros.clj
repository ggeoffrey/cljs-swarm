(ns swarm.macros
  "Where some witchcraft takes place. If you dare to enter this misterious place
   be sure to know the secret art of Abstract Syntax Tree manipulation.")


(defmacro limit
  "Generate a condition list that will keep a boid in bounds for a particular axis"
  [position & {:keys [using max-is varying-is]}]
  (let [setter using
        axis varying-is
        max-value max-is]
    `(cond ;; Below 0 or above the width on max-value
      (< ~axis (- ~max-value)) (~setter ~position ~max-value)
      (> ~axis ~max-value) (~setter ~position (- ~max-value)))))
