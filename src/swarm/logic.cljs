(ns swarm.logic
  "Where things start to look alive."
  (:require [swarm.tools :as t]
            [swarm.maths :as maths])
  (:require-macros [swarm.macros :refer [constraint]]))



(defn- local-group
  "Get the N nearest boids.
   Keep an eye on your neighbours ಠ_ಠ. Who knows what they are up to…"
  [boid all-boids n]
  (->> (map (fn [b] (list (maths/distance boid b) b))   ; Give ((0 boid₀) (23.78 boid₁) ...)
           all-boids)
       (sort-by first) ; sort by the first item in the tuples, i.e. by distance
       (rest)      ; skip the first which is equal to 0 -distance between a boid and itself is always 0-
       (take n)    ; keep only the N firsts
       (map last)  ; extract the boid, dropping the distance
       ))


(defn- gravity-center
  "Compute the gravity center of a group."
  [group]
  (maths/vector-avg (map (fn [boid] (.-position boid))
                         group)))


(defn- to-center
  "Try to stay close to other creatures, going to the local center. 
  Let's be social!"
  [boid group]
  ;; Trace a vector between the boid and the center
  (-> (maths/vector-to-target (.-position boid)
                              (gravity-center group)) 
      (.normalize) ;; make it a unit vector
      (.negate)  ;; reverse it
      ))


(defn- correct-position!
  "Keep the boids in the cube. As a divinity, we don't 
   want our creations to know what's behind the paradox.
   Nobody escape the Matrix."
  [boid w h d]
  (let [p (.-position boid)]
    (constraint p :on x :max (/ w 2) :min :opposite) ;; Is this sorcery ?
    (constraint p :on y :max (/ h 2) :min :opposite) ;; Yyyup! Totaly !
    (constraint p :on z :max (/ d 2) :min :opposite) ;; Look at the swarm.macro namespace
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
                              (map (fn [other] (list (maths/distance boid other) other))) ;; get the deltas
                              (filter (fn [tuple] (< (first tuple) min-distance)))  ;; keep the one that are too close
                              (map last)) ;; and keep only the boids, dropping the distance
       ]
    ;; ok so we have the neighbours we are going to hit
    (->> invading-friends
         (map (fn [friend] (.-position friend)))  ;; get their positions
         (cons (.-position boid))  ;; add the current boid's position
         (maths/vector-avg) ;; find the center
         (maths/vector-to-target (-> boid .-position)) ;; Δ from the current boid to this center
         (.normalize)  ;; make it a unit vector
           ;; Flee you fool ! To the oposite direction !
         )))


(defn smooth-direction
  "Force boids to try to match their neighbours direction"
  [boid group]
  (let [others-directions (map (fn [other] (.-direction other)) group)]
    (-> others-directions
        (maths/vector-avg)
        (.normalize))))


(defn- stay-near-center!
  "Make them stay around the center, like around a lightbulb"
  [boid]
  (.add (.-direction boid)  ;; add to the boid's direction
        ;; the inverted, reduced, normalized
        ;; vector towards the view's center
        (-> (maths/vector-to-target (.-position boid) 
                                    (maths/vector3))
            (.normalize)
            (.divideScalar 4)
            (.negate)))
  nil)

;; ------------------

(defn update-boids!
  "Make the boids swim or fly, or both… just imagine Peter!
   We are in Neverland after all!"
  [boids w h d options]
  (let [{:keys [max-speed min-distance neighbours]} options]  ;; extract parameters from options
    (doseq [b boids]   ;; foreach boid
      (let [group (local-group b boids neighbours)    ;; find his local group
            towards-center (to-center b group)        ;; get a vector towards the group's center
            to-avoid-others (avoid-contact b group 75)    ;; same to avoid others
            smoothed (smooth-direction b group)   ;; same to try to follow their directions
            direction (.-direction b)]

        ;; Add them all to the current boid's direction
        
        ;; Enable/disable steps or play with
        ;; the reduction factors to change
        ;; the boids' behaviour
        (doto direction
          (.add (.divideScalar towards-center    4    ))
          (.add (.divideScalar smoothed          5    ))
          (.add (.multiplyScalar to-avoid-others 1    ))
          )
        ;; apply this translation to the effective boid's position
        (.add (.-position b) direction))
      
      (correct-position! b w h d)  ;; Keep boids in the cube
      (stay-near-center! b)
      (limit-speed! b max-speed)
      )))  









