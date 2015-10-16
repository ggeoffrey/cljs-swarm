(ns swarm.view.view
  "Use THREE.js to generate a 3D view.
  @see http://threejs.org/"

  (:require [swarm.tools :as t]
            [swarm.view.boids :as boids]
            [swarm.logic :as logic]))





(defn- random-in-range
  "Give a random number in [-n/2..n/2]"
  [n]
  (-> (rand n)
      (- (/ n 2))))



(defn- get-random-pos
  "Return a random Vector3 within the borders"
  [width height depth]
  (new js/THREE.Vector3 (random-in-range width) (random-in-range height)  (random-in-range depth) ))


(defn- get-center
  "Return the center of the view based on its width, height and depth -respectivily-."
  [width height depth]
  (new js/THREE.Vector3 (/ width 2), (/ height 2), (/ depth 2)))



(defn- create-container
  "Create an inside-out Cube. Used as a container for the boids.
  The cube is colored inside and transparent from the outside."
  [width height depth]
  (let [center (get-center width height depth)
        geom (new js/THREE.CubeGeometry width height depth)
        material (new js/THREE.MeshBasicMaterial #js {"wireframe" false
                                                      "side" js/THREE.BackSide})
        cube (new js/THREE.Mesh geom material)
        position (.-position cube)]
    (.copy position center)
    cube))



(defn- draw-axes!
  "Draw the basic x, y and z axes in the scene
  Do not use THREE.AxisHelper on purpose"
  [scene]
  (let [red (new js/THREE.LineBasicMaterial #js {"color" 0xff0000})
        green (new js/THREE.LineBasicMaterial #js {"color" 0x00ff00})
        blue (new js/THREE.LineBasicMaterial #js {"color" 0x0000ff})
        x (new js/THREE.Geometry)
        y (new js/THREE.Geometry)
        z (new js/THREE.Geometry)
        ;; We set the zero-point to [1 1 1]. It make the axes visible inside the cube
        zero (new js/THREE.Vector3 1 1 1)]
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






(defn create-view
  "Create a view with a given state.
  A view is a set of three.js components instances.
  This is what appear inside you canvas."
  [state]
  (let [options @state

        ;;destructuring the app state
        {canvas "canvas"
         world-width "width"
         world-height "height"
         world-depth "depth"
         num-boids "boids"
         nb-neighbours "nb-neighbours"
         min-distance "min-distance"
         max-speed "max-speed"
         dev-mode "dev-mode"
         stats "stats"} options

        width (.-width canvas)
        height (.-height canvas)
        center (get-center world-width world-height world-depth)
        camera (new js/THREE.PerspectiveCamera 75 (/ width height) 0.1 100000)
        scene (new js/THREE.Scene)
        controls (new js/THREE.OrbitControls camera)
        renderer (new js/THREE.WebGLRenderer #js {"antialias" true
                                                  "canvas" canvas})

        container (create-container world-width world-height world-depth)
        boids (clj->js (boids/generate num-boids)) ;; we use a js-array to impove perfs.

        render (fn render []

                 (when (:run @state)
                   (.requestAnimationFrame js/window render)

                   (when-not (nil? stats)
                     (.begin stats))

                   (logic/update-boids! boids min-distance nb-neighbours max-speed)

                   (.render renderer scene camera)

                   (when-not (nil? stats)
                     (.end stats)))
                 )
        ]

    ;;  INIT defaults
    (.setSize renderer width height)
    (.setClearColor renderer 0x202020)
    (.set (.-position camera) 600 600 600)

    ;; SET the orbiting center to the center of the container
    (set! (.-target controls) (.-position container))
    ;; make the camera look at the center
    (.lookAt camera (.-position container))


    ;; ADD objects to the scene
    (draw-axes! scene)
    (.add scene container)

    (doseq [item boids]
      (.copy (.-position item) (get-random-pos world-width world-height world-depth))
      (.add container item))

    ; START the render loop
    (render)

    ; RETURN state and closures
    (let [callbacks {:stop  #(swap! state assoc :run false)
                     :start #(do (swap! state assoc :run true)
                               (render)
                               nil)}]
      (merge options callbacks))))









