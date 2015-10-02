(ns swarm.view.core
  (:require [swarm.tools :as t]))

(declare render)
(declare draw-axes!)


(defn create-view 
	"Create a with a given state"
	[state]
	(let [opt @state 
		{canvas :canvas
			world-width :width
			world-height :height
			num-boids :boids
			dev-mode :dev-mode
			stats :stats} opt
		camera (new js/THREE.PerspectiveCamera 75 (/ (.-width canvas) (.-height canvas)) 0.1 100000)
		scene (new js/THREE.Scene)
		controls (new js/THREE.OrbitControls camera)
		renderer (new js/THREE.CanvasRenderer #js {"antialias" true
													"canvas" canvas})
  		render (fn render []
             	
	            (when (:run @state)
					(.requestAnimationFrame js/window render)

					(when-not (nil? stats)
						(.begin stats))

					(.render renderer scene camera)

					(when-not (nil? stats)
						(.end stats)))
	 
				(when-not (:run @state)
						(t/log "should stop!")))
		]
		(draw-axes! scene)
		(render)
		(let [
        	callbacks 
			{:stop  #(swap! state assoc :run false)
			 :start #(do (swap! state assoc :run true)
						 (render)
       					 nil)}]
				(merge opt callbacks))))

	

(defn- draw-axes! 
  "Draw the basic x, y and z axes on the scene"
  [scene]
  (.add scene (new js/THREE.AxisHelper 5))
  nil)


