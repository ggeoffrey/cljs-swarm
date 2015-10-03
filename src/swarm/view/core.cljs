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
		{canvas "canvas"
			world-width "width"
			world-height "height"
   			world-depth "depth"
			num-boids "boids"
			dev-mode "dev-mode"
			stats "stats"} opt
  		width (.-width canvas)
    	height (.-height canvas)
     	center (get-center world-width world-height world-depth)
		camera (new js/THREE.PerspectiveCamera 75 (/ width height) 0.1 100000)
		scene (new js/THREE.Scene)
		controls (new js/THREE.OrbitControls camera)
		renderer (new js/THREE.WebGLRenderer #js {"antialias" true
													"canvas" canvas})
  
  		container (create-container world-width world-height world-depth)
        boids (boids/generate 40)
        boids-array (clj->js boids) ; used in the render loop for perfs
  		
  		
  		render (fn render []
             	
	            (when (:run @state)
					(.requestAnimationFrame js/window render)

					(when-not (nil? stats)
						(.begin stats))
     
     				
     				(logic/update-boids! boids-array world-width world-height world-depth)
         			
     				

					(.render renderer scene camera)

					(when-not (nil? stats)
						(.end stats))))
		]
   
   
   		(t/log canvas)
   
   		(.setSize renderer width height)
		(.setClearColor renderer 0x202020)
  
		(.set (.-position camera) 600 600 600)
  		(set! (.-target controls) (.-position container))
    	(.lookAt camera (.-position container))
  		
  		
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
     (new js/THREE.Vector3 (reduce-in-range width) (reduce-in-range height)  (reduce-in-range depth) )))
 
 (defn- create-container 
   "Create a wireframe Cube. Used as a container for the boids"
   [width height depth]
   (let [center (get-center width height depth) 
         geom (new js/THREE.CubeGeometry width height depth)
         material (new js/THREE.MeshBasicMaterial #js {"wireframe" false
                                                    "side" 1})
         cube (new js/THREE.Mesh geom material)
         position (.-position cube)]
     (.set position (:x center) (:y center) (:z center))
     cube))
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 


(defn- draw-axes! 
  "Draw the basic x, y and z axes on the scene
  Do not use AxisHelper on purpose"
  [scene]
  (let [red (new js/THREE.LineBasicMaterial #js {"color" 0xff0000})
        green (new js/THREE.LineBasicMaterial #js {"color" 0x00ff00})
        blue (new js/THREE.LineBasicMaterial #js {"color" 0x0000ff})
        x (new js/THREE.Geometry)
        y (new js/THREE.Geometry)
        z (new js/THREE.Geometry)
        zero (new js/THREE.Vector3 0 0 0)]
    (doto (.-vertices x)
      (.push zero)
      (.push (new js/THREE.Vector3 10000 0 0)))
    (doto (.-vertices y)
      (.push zero)
      (.push (new js/THREE.Vector3 0 10000 0)))
    (doto (.-vertices z)
      (.push zero)
      (.push (new js/THREE.Vector3 0 0 10000)))
    
    (.add scene (new js/THREE.Line x red))
    (.add scene (new js/THREE.Line y green))
    (.add scene (new js/THREE.Line z blue)))
  
  nil)


