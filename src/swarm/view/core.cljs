(ns swarm.view.core
  (:require [swarm.tools :as t]
            [swarm.view.boids :as boids]
            [swarm.logic.core :as logic]))

(declare render)
(declare draw-axes!)
(declare get-center )
(declare create-container)
(declare get-random-pos)


(defn create-view 
	"Create a with a given state"
	[state]
	(let [opt @state 
		{canvas :canvas
			world-width :width
			world-height :height
   			world-depth :depth
			num-boids :boids
			dev-mode :dev-mode
			stats :stats} opt
  		width (.-width canvas)
    	height (.-height canvas)
     	center (get-center world-width world-height)
		camera (new js/THREE.PerspectiveCamera 75 (/ width height) 0.1 100000)
		scene (new js/THREE.Scene)
		controls (new js/THREE.OrbitControls camera)
		renderer (new js/THREE.WebGLRenderer #js {"antialias" true
													"canvas" canvas})
  
  		container (create-container world-width world-height world-depth)
        boids (boids/generate 40)
  		
  		
  		render (fn render []
             	
	            (when (:run @state)
					(.requestAnimationFrame js/window render)

					(when-not (nil? stats)
						(.begin stats))
     
     				(logic/update-boids! boids world-width world-height world-depth)
         			
     				

					(.render renderer scene camera)

					(when-not (nil? stats)
						(.end stats))))
		]
   
   
   		(.setSize renderer width height)
		(.setClearColor renderer 0x202020)
  
		(.set (.-position camera) 300 300 300)
  		(.lookAt camera (new THREE.Vector3 0 0 0))
  
  		
		(draw-axes! scene)
  
  		;; code about the boids goes here
  		(.add scene container)
    
		(doseq [item boids]
    		(.copy (.-position item) (get-random-pos world-width world-height world-depth))
    		(.add container item))
    
  		; render 
		(render)
  
  		; return
		(let [
        	callbacks 
			{:stop  #(swap! state assoc :run false)
			 :start #(do (swap! state assoc :run true)
						 (render)
       					 nil)}]
				(merge opt callbacks))))

	
 
 (defn get-center 
   "Return the center of the view"
   [width height depth]
   {:x (- (/ width 2) 1)
    :y (- (/ height 2) 1)
    :z (- (/ depth 2) 1)  })
 
 
 (defn get-random-pos 
   "Return a random Vector3 within the borders"
   [width height depth]
   (let [reduce-in-range #(/ (- (rand (* %1 2)) %1) 2 )]
     (new THREE.Vector3 (reduce-in-range width) (reduce-in-range height)  (reduce-in-range depth) )))
 
 (defn- create-container 
   "Create a wireframe Cube. Used as a container for the boids"
   [width height depth]
   (let [center (get-center width height depth) 
         geom (new THREE.CubeGeometry width height depth)
         material (new THREE.MeshBasicMaterial #js {"wireframe" false
                                                    "side" 1})
         cube (new THREE.Mesh geom material)
         position (.-position cube)]
     (.set position (:x center) (:y center) (:z center))
     cube))
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 


(defn- draw-axes! 
  "Draw the basic x, y and z axes on the scene
  Do not use AxisHelper on purpose"
  [scene]
  (let [red (new THREE.LineBasicMaterial #js {"color" 0xff0000})
        green (new THREE.LineBasicMaterial #js {"color" 0x00ff00})
        blue (new THREE.LineBasicMaterial #js {"color" 0x0000ff})
        x (new THREE.Geometry)
        y (new THREE.Geometry)
        z (new THREE.Geometry)
        zero (new THREE.Vector3 0 0 0)]
    (doto (.-vertices x)
      (.push zero)
      (.push (new THREE.Vector3 10000 0 0)))
    (doto (.-vertices y)
      (.push zero)
      (.push (new THREE.Vector3 0 10000 0)))
    (doto (.-vertices z)
      (.push zero)
      (.push (new THREE.Vector3 0 0 10000)))
    
    (.add scene (new THREE.Line x red))
    (.add scene (new THREE.Line y green))
    (.add scene (new THREE.Line z blue)))
  
  nil)


