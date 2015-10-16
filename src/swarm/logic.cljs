(ns swarm.logic
  (:require [swarm.tools :as t])
  (:require-macros [swarm.macros :refer [def-]]))


;; TOOLS



(defn- distance-vec
  "Compute euclidian distance between two js/THREE.Vector3"
  [v1 v2]
  (.distanceTo v1 v2))

(defn- distance
  "Compute the euclidian distance between two boids"
  [boid1 boid2]
  (distance-vec (.-position boid1) (.-position boid2)))




(defn- get-closests
  "Return a sorted list of the n closests neighbours"
  [item others n]
  (let [distances (map (fn [other]
                         [ (.distanceTo (.-position item) (.-position other)) ,
                           other]) others)
        sorted (sort-by first distances)]
    (clj->js
     (mapv last (take n sorted)))))




;; Re-use a unique object to improve perfs
;; Acceptable since we are in a single threaded environnement
(def- center-shared-vector (new js/THREE.Vector3))

(defn- get-center
  "Give the center of the local group"
  [neighbours]
  (.set center-shared-vector 0 0 0)
  (doseq [other neighbours]
    (.add center-shared-vector (.-position other)))
  (.divideScalar center-shared-vector (count neighbours)))


;; EXTRA


(defn- limit-speed!
  "Limit the speed of a boid by reducing the velocity"
  [velocity max-speed]
  (let [length (.length velocity)]
    (if (> length max-speed)
      (do
        (-> (.normalize velocity)
            (.multiplyScalar max-speed)))
      (do
        velocity))))



(def- center (new js/THREE.Vector3 100 100 100))

(defn- stay-in-space
  "Force tho boids to stay near the center"
  [item]
  (-> (.clone center)
      (.sub (.-position item))
      (.divideScalar 1000)))


(def- wind-default (-> (new js/THREE.Vector3  -10 -10 -10)
                      (.divideScalar 100)))

(defn- wind
  "Return a wind force"
  []
  wind-default)





;; RULES


(defn- go-to-center
  "Make a boid move to the center of his group one step
  Compute the diff Vector between the boid and the center then add 1/100 of its length to the direction
  for a smooth turn"
  [item center]
  (-> (.clone center)
      (.sub (.-position item))
      (.divideScalar 100)))




(def- shared-position (new js/THREE.Vector3))

(defn- avoid-collision
  "Avoid collision with other boids"
  [item others min-distance]
  (let [pos (.-position item)]
    (.set shared-position 0 0 0)
    (doseq [other others]
      (when-not (= item other)
        (let [distance (.distanceTo pos (.-position other))
              diff (-> (.clone pos)
                       (.sub (.-position other))
                       (.divideScalar 5)
                       )]
          (when (< distance min-distance)
            (.sub shared-position diff)))
        ))
    (.negate shared-position)
    (.divideScalar shared-position 5)
    shared-position))





;; Re-use a unique object to improve perfs
;; Acceptable since we are in a single threaded environnement
(def- avg-shared-vector (new js/THREE.Vector3))

(defn follow-velocity
  "Set the velocity to the average of the group"
  [item others]
  (.set avg-shared-vector 0 0 0)
  (doseq [other others]
    (when-not (= item other)
      (.add avg-shared-vector (.-velocity other))))

  (-> avg-shared-vector
      (.divideScalar (dec (count others)))
      (.sub (.-velocity item))
      (.divideScalar 8)))





;; APPLY RULES



(defn- set-position!
  "Change position of an item"
  [item others min-distance nb-neighbours max-speed]
  (let [neighbours (get-closests item others nb-neighbours)
        center (get-center neighbours)

        v1 (go-to-center item center)
        v2 (avoid-collision item neighbours min-distance)
        v3 (follow-velocity item neighbours)
        center (stay-in-space item)
        wind (wind)
        velocity (-> (.-velocity item)
                     (.add v1)
                     (.add v2)
                     (.add v3)
                     (.add center)
                     (.add wind)
                     (limit-speed! max-speed))]

    ;(set! (.-velocity item) velocity)
    (.add (.-position item) velocity)))





(defn update-boids!
  "Use swarm intelligence to make tho boids move in a closed space."
  [boids min-distance nb-neighbours max-speed]
  (doseq [item boids]
    (set-position! item boids min-distance nb-neighbours max-speed)
    ))
