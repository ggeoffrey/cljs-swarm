(ns swarm.logic.core
  "Hold the swarm intelligence rules.
  The (update-boids!) function take a list of boids and make them move according to the rules."
  (:require [swarm.tools :as t]))

(declare set-position!)
(declare get-closests)
(declare get-center)

(declare go-to-center)
(declare avoid-collision)
(declare follow-velocity)


(declare limit-speed)
(declare stay-in-space)
(declare wind)


(def min-distance 60)  ;; TODO  min-distance-parameter

(defn update-boids!
  "Use swarm intelligence to make tho boids move in a closed space.
  Modify the boids position, causing side-effect -hence the BANG '!' sign-."
  [boids width height depth]
  (let [size (dec (.-length boids))] ;; O(1)
    (loop [i 0]  ;; O(n)
      (set-position! (aget boids i) width height depth boids)  ;; O(?)
      
      (when (< i size)
        ;; Tail call optimisation
        (recur (inc i))))))




(defn set-position! 
  "Change a boid's position by side effect.
  - item : item to move
  - width 
  - height 
  - depth 
  - others : list of all boids -including item-
  "
  [item width height depth others]
  
  (let [  pos (.-position item)
          x (.-x pos)
          y (.-y pos)
          z (.-z pos)
          neighbours (get-closests item others 5)  ;; O(x*log(x)+x+2n)  ;; TODO - nb-neighbors-parameter
          center (get-center neighbours)           ;; O(n),  n = (count neighbours)
          ]
      
      (let [rule1 (go-to-center item center)
            rule2 (avoid-collision item neighbours)
            rule3 (follow-velocity item neighbours)
            center (stay-in-space item)
            wind (wind)]
        
        (-> (.-velocity item)
            (.add rule1)
            (.add rule2)
            (.add rule3)
            (.add center)
            ;(.add wind)
            (limit-speed))
        
        (.add (.-position item) (.-velocity item))
        ;(.add (.-position item) wind)
        )))


(defn get-closests 
  "Return a sorted list of the n closests neighbours.
  This is the hotspot of the application.
  This function is a little bit esotheric because it use javascript witchcraft to improve performances.
  The goal here is to avoid new instanciations and to avoid overhead will providing a good time complexity.
  - Array.length = 0 -> empty array
  - Native arrays are faster than clojure immutable data structures.
  This function:
  - Compute the distance between each boids and the current boid : O(x)
  - Sort by distance : O(x*log(x))
  - Take n neighbours : O(n)
  - Fill a js-array of the n closests neighbours : O(n)
  - Return this array
  x beeing the total number of boids -size-.
  n beeing the number of neighbours to take.
  Total complexity : O(x)+O(x*log(x))+O(n)+O(n) = O(x*log(x)+x+2n)"
  [item others n]
  
  (let [distances (array)  ;; use array for perf
        size (dec (.-length others))]
    (loop [i 0]  ;; use loop for perfs   O(size)
      (when-not (= (aget others i) item)
        (.push distances #js [ (.distanceTo (.-position item), (.-position (aget others i))),   (aget others i)])) ;; use array for perf
      (when (< i size)
        (recur (inc i))))
    
    (let [sorted (sort-by first distances)  ;; O(size*log(size))
          neighbours (take n sorted)        ;; O(n)
          neig-list distances]
      (set! (.-length neig-list) 0)  ;; re-use distances array : neig-list.length=0 -> empty array
      (doseq [neig neighbours]
        (.push neig-list (last neig)))
      neig-list)))


;; Re-use a unique object to improve perfs
;; Acceptable since we are in a single threaded environnement
(def ^:private center-shared-vector (new js/THREE.Vector3)) 

(defn- get-center 
  "Give the center of the boid's neighbourhood."
  [neighbours]
  (.set center-shared-vector 0 0 0)
  (let [n (.-length neighbours)]
    (doseq [boid neighbours]
      (.add center-shared-vector (.-position boid)))
    (.divideScalar center-shared-vector n)
    center-shared-vector))




(defn go-to-center
  "Compute the diff Vector between the boid and the center then add 1/100 of its length to the direction
  for a smooth turn"
  [item center]
  (let [pos (.-position item)
        diff (-> (.clone center)
          (.sub pos)
          (.divideScalar 100))]
    diff))


;; shared vector for perfs -no side-effect-
(def shared-collision-vect (new js/THREE.Vector3))
(defn avoid-collision
  "Avoid collision with other boids"
  [item others]
  (let [pos (.-position item)]
    (.set shared-collision-vect 0 0 0)
    (doseq [other others]
      (when-not (= item other)
        (let [distance (.distanceTo pos (.-position other))]
          (when (< distance min-distance)
            (let [diff (-> (.clone pos)
                       (.sub (.-position other))
                       ;(.multiplyScalar 5)
                       )]
              (.sub shared-collision-vect diff)))
        )))
    (.negate shared-collision-vect)
    (.divideScalar shared-collision-vect 100)
    shared-collision-vect))


;; Re-use a unique object to improve perfs
;; Acceptable since we are in a single threaded environnement
(def ^:private shared-velocity-vect (new js/THREE.Vector3)) 

(defn follow-velocity
  "Set the velocity to the average of the group"
  [item others]
  (.set shared-velocity-vect 0 0 0)
  (doseq [other others]
    (when-not (= item other)
      (.add shared-velocity-vect (.-velocity other))))
  (.divideScalar shared-velocity-vect (dec (count others)))
  (let [corrected (-> shared-velocity-vect
                      (.negate)
                      )]
    corrected))



(defn limit-speed 
  "Limit the speed of a boid by reducing the velocity"
  [velocity]
  (let [length (.length velocity)
        attenuation 3]
    (if (> length attenuation)
      (do
        (-> (.normalize velocity)
            (.multiplyScalar attenuation)))
      (do
        velocity))))


(def center (new js/THREE.Vector3 -10 -10 -10))

(defn stay-in-space 
  "Force tho boids to stay near the center"
  [item]
  (-> (.clone center)
      (.sub (.-position item))
      (.divideScalar 100)))


(def wind-default (-> (new js/THREE.Vector3  1 1 1)
                      ;(.divideScalar 2)
                      ))

(defn wind
  "Return a wind force"
  []
  wind-default)

