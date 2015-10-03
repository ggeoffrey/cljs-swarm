(ns swarm.logic.core
  (:require [swarm.tools :as t]))

(declare set-position!)
(declare get-closests)
(declare get-center)
(declare go-to-center!)
(declare avoid-collision!)

(declare avoid-escape!)
(declare move!)



(def min-distance 20)

(defn update-boids!
  "Use swarm intelligence to make tho boids move in a closed space."
  [boids width height depth]
  (loop [i 0
         size (dec (count boids))
         items boids]
    (let [item (first items)]
      (set-position! item width height depth boids)
      )
    (when (< i size)
      (recur (inc i) size (next items)))))


(defn set-position! 
	"Change position of an item"
	[item width height depth others]
	(let [  pos (.-position item)
       		x (.-x pos)
			y (.-y pos)
			z (.-z pos)
   			neighbours (get-closests item others 5)
      		center (get-center neighbours)]
   		
   		(go-to-center! item center)
     	;(avoid-collision! item neighbours)
      	(move! item)
      	(avoid-escape! item width height depth)
   	))


(defn get-closests 
  "Return a sorted list of the n closests neighbours"
  [item others n]
  (let [distances (doall (map (fn [other]
                                [ (.distanceTo (.-position item) (.-position other))
                                 other]) others))
        sorted (sort-by first distances)]
    (mapv last (take n sorted))))


(def center-shared-vector (new js/THREE.Vector3)) ;; Re-use a unique object to improve perfs
(defn- get-center 
  "doc-string"
  [neighbours]
  (let [n (count neighbours)
        [x y z] (loop [i 0
                       x 0
                       y 0
                       z 0]
                  
                    (let [pos (.-position (nth neighbours i))]
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
        diff (-> (.clone pos)
                (.sub center)
                (.normalize)
                )]
    (-> dir
        (.sub diff)
        (.normalize))
    
    
    ))


(defn avoid-collision!
  "Avoid collision with other boids"
  [item others velocity]
  (let [pos (.-position item)]
    (doseq [other others]
      (let [other-pos (.-position other)
            distance (.distanceTo pos other-pos)]
        ;(set! (.-velocity item) (/ (+ (.-velocity item) (.-velocity other)) 2))
        (when (< distance min-distance )
          (let [clone (.clone pos)
                sum (.sub clone other-pos)
                norm (.normalize sum)
                negated (.negate norm)
                final (.multiplyScalar negated (.-velocity item))]
            (.sub pos negated)))))))


(defn avoid-escape!
  [item width height depth]
  
  (let [pos (.-position item)
        dir (.-direction item)]
    
    ; when the boid go outside the box on X, negate x direction
    (when (or
            (> (.-x pos) (/ width 2))
            (< (.-x pos) (- (/ width 2))))
      (.set dir (- (.-x dir)) (.-y dir) (.-z dir)))
    
    ; when the boid go outside the box on Y, negate y direction
    (when (or
            (> (.-y pos) (/ height 2))
            (< (.-y pos) (- (/ height 2))))
      (.set dir (.-x dir)  (- (.-y dir)) (.-z dir)))
    
    ; when the boid go outside the box on Z, negate z direction
    (when (or
            (> (.-z pos) (/ depth 2))
            (< (.-z pos) (- (/ depth 2))))
      (.set dir (.-x dir) (.-y dir) (- (.-z dir)) ))
    ))