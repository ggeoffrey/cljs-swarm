(ns swarm.logic.core
  (:require [swarm.tools :as t]))

(declare set-position!)
(declare get-closests)
(declare get-center)
(declare go-to-center!)
(declare avoid-collision!)

(declare avoid-escape!)
(declare move!)

(declare follow-velocity!)
(declare limit-speed)
(declare  stay-in-space)
(declare wind)


(def min-distance 30)

(defn update-boids!
  "Use swarm intelligence to make tho boids move in a closed space."
  [boids width height depth]
  (let [size (dec (.-length boids))]
    (loop [i 0]
      (set-position! (aget boids i) width height depth boids)
      (when (< i size)
        (recur (inc i))))))


(defn set-position! 
	"Change position of an item"
	[item width height depth others]
	(let [  pos (.-position item)
       		x (.-x pos)
			    y (.-y pos)
			    z (.-z pos)
   			  neighbours (get-closests item others 5)
      		center (get-center neighbours)]
   		
      
      (let [v1 (go-to-center! item center)
            v2 (avoid-collision! item neighbours)
            v3 (follow-velocity! item neighbours)
            center (stay-in-space item)
            wind (wind)
            velocity (-> (.-direction item)
                       	  (.add v1)
                          (.add v2)
                          (.add v3)
                          (.add center)
                          (.add wind)
                          (limit-speed))]
        
        (set! (.-direction item) velocity)
        (.add pos velocity)
        ;(follow-group! item neighbours)
       	;(avoid-escape! item width height depth))
        )))


(defn get-closests 
  "Return a sorted list of the n closests neighbours"
  [item others n]
  (let [distances (doall (map (fn [other]
                                [ (.distanceTo (.-position item) (.-position other))
                                 other]) others))
        sorted (sort-by first distances)]
    (clj->js
     (mapv last (take n sorted)))))


;; Re-use a unique object to improve perfs
;; Acceptable since we are in a single threaded environnement
(def center-shared-vector (new js/THREE.Vector3)) 

(defn- get-center 
  "doc-string"
  [neighbours]
  (let [n (count neighbours)
        [x y z] (loop [i 0
                       x 0
                       y 0
                       z 0]
                  
                    (let [pos (.-position (aget neighbours i))]
	                    ;(t/log pos)
	                    (if (< i (dec n))
	                    	(recur (inc i) (+ x (.-x pos)) (+ y (.-y pos)) (+ z (.-z pos)))
	                     	[(+ x (.-x pos)) (+ y (.-y pos)) (+ z (.-z pos))]))
                    )
        cx (/ x n)
        cy (/ y n)
        cz (/ z n)]
    (.set center-shared-vector cx cy cz) 
    center-shared-vector))



(defn move!
  "Move a boid by applying transformation on his .-position Vector3"
  [boid]
  (.add (.-position boid) (.-direction boid)))


(defn go-to-center!  ;; TODO add velocity parameter
	"Make a boid move to the center of his group one step
	Compute de diff Vector between the boid and the center then add 1/100 of its length to the direction
	for a smooth turn"
	[item center]
	(let [dir (.-direction item)
		pos (.-position item)

		diff (-> (.clone center)
			(.sub pos)
			(.divideScalar 50))]

		diff
		))

(def shared-velocity (new js/THREE.Vector3))

(defn avoid-collision!
  "Avoid collision with other boids"
  [item others]
  (let [pos (.-position item)]
    (.set shared-velocity 0 0 0)
    (doseq [other others]
      (when-not (= item other)
        (let [distance (.distanceTo pos (.-position other))
              diff (-> (.clone pos)
                       (.sub (.-position other))
                       (.divideScalar 5)
                       )]
			(when (< distance min-distance)
     			(.sub shared-velocity diff)))
        ))
    ;(t/log shared-velocity)
    ;(.divideScalar shared-velocity 2)
    (.negate shared-velocity)
    (.divideScalar shared-velocity 5)
    shared-velocity))


;; Re-use a unique object to improve perfs
;; Acceptable since we are in a single threaded environnement
(def avg-shared-vector (new js/THREE.Vector3)) 

(defn follow-velocity!
  "Set the velocity to the average of the group"
  [item others]
  (.set avg-shared-vector 0 0 0)
  (doseq [other others]
    (when-not (= item other)
      (.add avg-shared-vector (.-direction other))))
  (.divideScalar avg-shared-vector (dec (count others)))
  (let [corrected (-> avg-shared-vector
                      (.sub (.-direction item))
                      (.divideScalar 8))]
    corrected))



(defn limit-speed 
  "Limit the speed of a boid by reducing the velocity"
  [velocity]
  (let [length (.length velocity)]
    (if (> length 3)
      (do
        (-> (.normalize velocity)
            (.multiplyScalar 3)))
      (do
        velocity))))

(def center (new js/THREE.Vector3 100 100 100))

(defn stay-in-space 
  "Force tho boids to stay near the center"
  [item]
  (-> (.clone center)
      (.sub (.-position item))
      (.divideScalar 1000)))


(def wind-default (-> (new js/THREE.Vector3  -10 -10 -10)
                      (.divideScalar 100)))

(defn wind
  "Return a wind force"
  []
  wind-default)


;; Correct the dirce
(defn avoid-escape!
	"Correct the direction of a boid  if its trying to run away"
	[item width height depth]

	(let [pos (.-position item)
		dir (.-direction item)
  		
    	w (/ width 2)
     	h (/ height 2)
      	d (/ depth 2)
       ]

    ; when the boid go outside the box on X, correct x direction   
    
   (when (> (.-x pos) (- w min-distance))
     (.set dir (- (- w (.-x pos)) min-distance) (.-y dir) (.-z dir)))
   (when (< (.-x pos) (+ (- w) min-distance))
     (.set dir (+ (- (- w) (.-x pos)) min-distance)   (.-y dir) (.-z dir)))
   

   ; when the boid go outside the box on Y, correct y direction
   (when (> (.-y pos) (- h min-distance))
     (.set dir  (.-x dir) (- (- h (.-y pos)) min-distance) (.-z dir)))
   (when (< (.-y pos) (+ (- h) min-distance))
      (.set dir    (.-x dir)	(+ (- (- h) (.-y pos)) min-distance)  (.-z dir)))
    
    
    
    ; when the boid go outside the box on Z, correct z direction
    (if (> depth (* 3 min-distance))
      
      (do
        (when (> (.-z pos) (- d min-distance))
      	  (.set dir  (.-x dir) (.-y dir)  (- (- d (.-z pos)) min-distance) ))
        (when (< (.-z pos) (+ (- d) min-distance))
      	  (.set dir  (.-x dir) (.-y dir) (+ (- (- d) (.-z pos)) min-distance) )))
      (do 
        (.set dir (.-x dir) (.-y dir) 0 )
        (.set pos (.-x pos) (.-y pos) 0 )))
    
    
    
    ;(.multiplyScalar dir 2)
    ))