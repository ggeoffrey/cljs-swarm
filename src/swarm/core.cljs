(ns ^:figwheel-always  swarm.core
  "Entry point of the application.
  Use the figwheel library for live coding.
  If you are here to see how the swarm work then look at swarm.logic.core.
  For the 3D part look at the swarm.view namespace.
  "
  (:require [swarm.tools :as t]
            [swarm.view.core :as view]
            [cljs.core :as core ])
  (:require-macros [swarm.macros :refer [def-]]))



(enable-console-print!)

(declare main)
(declare clean-parameters!)
(declare check-field-number!)


;; Default options
(def- default-values {"width" 600
                                   "height" 600
                                   "depth" 600
                                   "boids" 60
                                   "nb-neighbours" 5
                                   "min-distance" 30
                                   "max-speed" 3
                                   "stats" (t/create-stats)
                                   :run true})

;; Application state. thread safe with atomic modifications.
;; Hold parameters and instances between each code recompilation.
;; Allow the application to perform "hot code push".
(defonce- state (atom {}))

(def- hot-code-mode false)

(defn on-js-reload
  "Called by the figwheel library when the new compiled code is pushed"
  []

  (when-not hot-code-mode
    ;; Stop the rendering loop by getting the "stop" closure from the state and calling it.
    (let [stop (get @state :stop)]
      (stop))

    ;; Wait 100ms
    ;; Set the state at runnable=true
    ;; Call the (main) function with the old state.
    ;; (main) will reuse the old state and won't regenerate everything.
    (js/setTimeout (fn []   ;; wait for the requestAnimationFrame to end
                     (swap! state assoc :run true)
                     (let [new-state (main state)]  ;; restart with the previous state
                       (reset! state new-state))  ;; keep new state
                     ), 100)))


;; Only exported -public- function.
(defn ^:export create
  "Create a swarm according to the 'options' parameter.
  If 'options' is empty or invalid, the default options will be used and a warning will be
  logged if the console.
  You must provide options with at least a 'canvas' field ({canvas: CanvasElement}).
  @see swarm.core/default-values"
  [options]

  (if (nil? options)
    (throw (new js/Error "You must provide options with at least a 'canvas' field ({canvas: CanvasElement})")))
  (let [options (js->clj options)]
    (if (options "dev-mode")
      (do
        ;; If we are in dev mode then merge this options with the old ones -and keep the newests-
        (reset! state (merge default-values @state options))
        ;; call main and replace the old state with the new state
        (reset! state (main state))
        ;; return a JS object to the user containing the application state and the closures.
        (clj->js @state))
      (do
        ;; If we are not in dev mode, create a new empty state. This state is side-effect free
        ;; and do not allow live-coding. Each instance of a swarm will have its own state.
        (let [state (atom (merge default-values options))]
          ;; Call main and return to the user a JS object containing the swarm state and the closures.
          (clj->js (main state)))))))



(defn- main
  "Main entry point
  options is a map and need:
  - a Canvas object
  - a number of boids (default is 20)
  - a width     (default is 600)
  - an height     (default is 600)
  - an depth     (default is 600)
  "
  [state]
  (clean-parameters! state)
  (view/create-view state))




(defn- clean-parameters!
  "Check the app state atom to detect missing or incorrect parameters and fix it -overwrite them-."
  [state]
  ;; If the provided canvas is not a real canvas
  (when-not (t/canvas? (@state "canvas"))
    (throw (new js/Error (str "A valid canvas is needed. Got '"  (@state "canvas") "'" ))))


  (check-field-number! state "width" default-values)
  (check-field-number! state "height" default-values)
  (check-field-number! state "boids" default-values))


(defn- check-field-number!
  "Check a numeric field and set its value to the default if not correct.
  A value is correct if it is not nil, a number and higher then 0.
  If a parameter is overwritten, a warning will be logged in the console.
  "
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
