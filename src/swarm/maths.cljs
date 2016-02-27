(ns swarm.maths
  "Everything related to the universe's whispers")


(defn vector3
  ([]
   (vector3 0 0 0))
  ([x y z]
   (js/THREE.Vector3. x y z)))


(defn square
  "Put x to the square"
  [x]
  (* x x))


(defn- distance-vectors
  "Distance between two vector3"
  [v1 v2]
  (.distanceTo v1 v2))



(defn distance
  "Compute the euclidian distance between two boids"
  [b1 b2]
  (distance-vectors (-> b1 .-position)
                    (-> b2 .-position)))



(defn vector-sum
  "Compute the sum of some vectors"
  [vecs]
  (loop [accu (vector3) ;; Accumulator, a zero vector
         vecs vecs]       ;; The current list
    (cond
     (nil? (first vecs)) accu   ;; Have we finished the list ? if it's empty then yes return what we did
     :else  ;; add to the accu and do it again with the remainings
         (recur (.add accu (first vecs)) (rest vecs)))))



(defn vector-to-target
  "Create a vector pointing to a particular point. 
  E.g. from a boid to the local group gravity center.  o--->o "
  [source target]
  (doto (vector3)   ;; Take a zero vector
    (.add source)     ;; make it equal to the origin
    (.sub target)))   ;; and substract the target. Tada !




(defn random-in-range
  "Give a random number in [-n/2..n/2]"
  [n]
  (- (rand n) (/ n 2)))


(defn random-position 
  "Give a random point in a given range -width height depth respectively-"
  [w h d]
  (apply vector3 (map random-in-range (list w h d))))


(defn- center3
  "Return the center of width, height and depth -respectivily-."
  [w h d]
  (apply vector3 (map (fn [x] (/ x 2))
                      (list w h d))))
