(ns swarm.logic
  "Where things start to look alive."
  (:require [swarm.tools :as t])
  (:require-macros [swarm.macros :refer [limit]]))


(defn- square
  "Put x to the square"
  [x]
  (* x x))


(defn- distance
  "Compute the euclidian distance between two boids"
  [b1 b2]
  (let [p1 (-> b1 .-position)
        p2 (-> b2 .-position)
        x1 (.-x p1)
        y1 (.-y p1)
        z1 (.-z p1)
        x2 (.-x p2)
        y2 (.-y p2)
        z2 (.-z p2)]
    (Math.sqrt
     (+
      (square (- x2 x1))
      (square (- y2 y1))
      (square (- z2 z1))))))

(defn- vector-sum
  "Compute the sum of some vectors"
  [vecs]
  (loop [accu (t/vector3) ;; Accumulator, a zero vector
         vecs vecs]       ;; The current list
    (cond
     (nil? (first vecs)) accu   ;; Have we finished the list ? if it's empty then yes return what we did
     :else  ;; add to the accu and do it again with the remainings
         (recur (.add accu (first vecs)) (rest vecs)))))

(defn- local-group
  "Get the N nearest boids.
   Keep an eye on your neighbours ಠ_ಠ. Who knows what they are up to…"
  [boid all-boids n]
  (->> (map (fn [b] (list (distance boid b) b))   ; Give ((0 boid₀) (23.78 boid₁) ...)
           all-boids)
       (sort-by first) ; sort by the first item in the tuples, i.e. by distance
       (rest)      ; skip the first which is equal to 0 -distance between a boid and itself is always 0-
       (take n)    ; keep only the N firsts
       (map last)  ; extract the boid, dropping the distance
       ))


(defn- gravity-center
  "Compute the gravity center of a group."
  [group]
  (vector-sum (map (fn [boid] (-> boid .-position))
                   group)))


(defn- vector-to-target
  "Create a vector pointing to a particular point. 
  E.g. from a boid to the local group gravity center.  o--->o "
  [source target]
  (doto (t/vector3)   ;; Take a zero vector
    (.add source)     ;; make it equal to the origin
    (.sub target)))   ;; and substract the target. Tada !


(defn- to-center
  "Try to stay close to other creatures, going to the local center. 
  Let's be social!"
  [boid group]
  (let [center (gravity-center group)     ;; get the group's center
        reduced-direction (-> (vector-to-target (-> boid .-position) center) ;; Trace a vector between the boid and the center
                              (.normalize))]                  ;; make it a unit vector
    reduced-direction))


(defn- correct-position!
  "Keep the boids in the cube. As a divinity, we don't 
   want our creations to know what's behind the paradox.
   Nobody escape the Matrix."
  [boid w h d]
  (let [p (-> boid .-position)]
    (limit p :using .setX :max-is (/ w 2) :varying-is (.-x p)) ;; Is this sorcery ?
    (limit p :using .setY :max-is (/ h 2) :varying-is (.-y p)) ;; Yyyup! Totaly !
    (limit p :using .setZ :max-is (/ d 2) :varying-is (.-z p)) ;; Look at the swarm.macro namespace
    )  
  nil)

(defn- limit-speed!
  "Limit a boid's speed. We don't want to create plasma."
  [b max-speed]
  (when (> (.length (.-direction b)) max-speed)
    (-> (.normalize (.-direction b))
        (.multiplyScalar max-speed))))


(defn avoid-contact
  "Avoid collision with others.
   I've never seen a bird sitting on an other's 
   back while flying. Did you? Yes? You lucky bastard !"
  [boid group min-distance]
  (let [direction (-> boid .-direction) ;; get the direction
        invading-friends (->> group
                              (map (fn [other] (list (distance boid other) other))) ;; get the deltas
                              (filter (fn [tuple] (< (first tuple) min-distance)))  ;; keep the one that are too close
                              (map last)) ;; and keep only the boids, dropping the distance
       ]
    ;; ok so we have the neighbours we are going to hit
    (->> invading-friends
         (map (fn [friend] (-> friend .-position)))  ;; get their positions
         (cons (-> boid .-position))  ;; add the current boid's position
         (vector-sum) ;; find the center
         (vector-to-target (-> boid .-position)) ;; Δ from the current boid to this center
         (.normalize)  ;; make it a unit vector
         (.negate)  ;; Flee you fool ! To the oposite direction !
         )))


(defn smooth-direction
  "Force boids to try to match their neighbours direction"
  [boid group]
  (let [others-directions (map (fn [other] (-> other .-direction)) group)]
    (-> others-directions
        (vector-sum)
        (.normalize))))

;; ------------------

(defn update-boids!
  "Make the boids swim or fly, or both… just imagine Peter!
   We are in Neverland after all!"
  [boids w h d options]
  (let [{:keys [max-speed min-distance neighbours]} options]  ;; extract parameters from options
    (doseq [b boids]   ;; foreach boid
      (let [group (local-group b boids neighbours)    ;; find his local group
            towards-center (to-center b group)        ;; get a vector towards the group's center
            to-avoid-others (avoid-contact b group min-distance)    ;; same to avoid others
            smoothed (smooth-direction b group)   ;; same to try to follow their directions
            direction (-> b .-direction)]

        ;; Add them all to the current boid's direction
        
        ;; Enable/disable steps or play with
        ;; the reduction factors to change
        ;; the boids' behaviour
        (doto direction
          (.add (.divideScalar towards-center    4    ))
          (.add (.divideScalar to-avoid-others   0.25 ))
          (.add (.divideScalar smoothed          5    )))
        ;; apply this translation to the effective boid's position
        (.add (-> b .-position) direction))
      
      (correct-position! b w h d)  ;; Keep boids in the cube
      (limit-speed! b max-speed))))  









