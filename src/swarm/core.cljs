(ns ^:figwheel-always swarm.core
    (:require [swarm.tools :as t]
              [swarm.view.core :as view]
              [cljs.core :as core ]))

(enable-console-print!)

(declare main)
(declare clean-parameters!)
(declare check-field-number!)


(defonce default-values {"width" 600
                         "height" 600
                         "depth" 600
                         "boids" 20
                         "positions" []
                         "stats" (t/create-stats)
                         :run true})

(defonce state (atom {}))


(defn on-js-reload []
  
  (let [stop (:stop @state)]
    (stop))
  
  (js/setTimeout (fn []   ;; wait for the requestAnimationFrame to end
  	(swap! state assoc :run true)
  	(let [new-state (main state)]  ;; restart with the previous state
    	(reset! state new-state))  ;; keep new state
  ), 100)
   
)

(defn ^:export create 
  "Create a swarm"
  [options]
  (if (nil? options)
    (throw (new js/Error "You must provide options with at least a 'canvas' field ({canvas: CanvasElement})")))
  (let [options (js->clj options)] 
    (if (options "dev-mode")
      (do
        (reset! state (merge default-values @state options))
        (reset! state (main state))
        (clj->js @state))
      (do
        (let [state (atom (merge default-values options))]
          (clj->js (main state)))))))

(defn- main 
  "Main entry point
  options is a map and need:
  	- a Canvas object
  	- a number of boids (default is 20)
  	- a width			(default is 600)
  	- an height			(default is 300)
  	optionaly:
  	- a list of positions like [[x y] [x y]...] if this parameter is specified, a boid will
  		be created for each position. So the 'number of boids' parameter will be ignored.
  "
  [state]
 
  (clean-parameters! state)
  
  (view/create-view state)
  )




(defn clean-parameters!
	"Check the app state to detect missing or incorrect parameters and fix it"
	[state]
  	
	(when-not (t/canvas? (@state "canvas")) 
		(throw (new js/Error (str "A valid canvas is needed. Got '"  (@state "canvas") "'" ))))

	(check-field-number! state "width" default-values)
	(check-field-number! state "height" default-values)
	(check-field-number! state "boids" default-values))


(defn check-field-number!
  "Check a field and set a value to the default if not correct"
  [state key default]
  (let [item (get @state key)
        key (keyword key)]
    (when 
    	(or 
      		(nil? item)
        	(not (number? item))
        	(<= item 0))
     	(swap! state assoc key (get default key))
      	
      	(t/warn (str "Parameter " key " as been replaced by value " (default key))))))