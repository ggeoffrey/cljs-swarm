(ns swarm.view
  "Everything to build the view. Using THREE.JS."
  (:require [swarm.tools :as t]
            [swarm.maths :as maths]
            [swarm.boids :as boids]
            [swarm.logic :as logic]))


(defn- container3
  "Create an inside-out Cube. Used as a container for the boids.
  The cube is colored inside and transparent from the outside.
  NOTE:  The cube is currently transparent and replaced by a plane."
  [w h d]
  (let [container  (js/THREE.Mesh.
                    (js/THREE.CubeGeometry. w h d)
                    (js/THREE.MeshLambertMaterial. #js {:visible false
                                                        :side js/THREE.BackSide
                                                        :color 0xFFFFFF}))]
    (.copy (.-position container) (maths/center3 w h d))
    (set! (.-receiveShadow container) true)
    container))



(defn- draw-axes! 
  "Draw x, y and z axes in the scene. Do not use THREE.AxisHelper on purpose."
  [scene]
  ;; Snif snif … it smells like imperative code ! Ew !
  (let [x (js/THREE.Geometry.)
        y (js/THREE.Geometry.)
        z (js/THREE.Geometry.)
        ;; We set the zero-point to [1 1 1]. It make the axes visible inside the cube
        zero (maths/vector3 1 1 1)]
    (doto (.-vertices x)
      (.push zero)
      (.push (maths/vector3   7000 0 0)))
    (doto (.-vertices y)
      (.push zero)
      (.push (maths/vector3   0 7000 0)))
    (doto (.-vertices z)
      (.push zero)
      (.push (maths/vector3   0 0 7000)))
    (.add scene (js/THREE.Line. x
                                (js/THREE.LineBasicMaterial. #js {:color 0xff0000})))
    (.add scene (js/THREE.Line. y
                                (js/THREE.LineBasicMaterial. #js {:color 0x00ff00})))
    (.add scene (js/THREE.Line. z
                                (js/THREE.LineBasicMaterial. #js {:color 0x0000ff})))))


(defn- light
  "Create a custom SpotLight."
  []
  (let [color (js/THREE.Color. 0xFFFFFF)
        shadow-map 4096
        light (js/THREE.SpotLight. color 2.0)]
    (.set (.-position light) 4000 8000 4000)
    (set! (-> light .-shadow .-mapSize .-width) shadow-map)
    (set! (-> light .-shadow .-mapSize .-height) shadow-map)
    (set! (-> light .-shadow .-camera .-far) 10000)
    (set! (.-castShadow light) true)
    (.lookAt light (maths/vector3 4000 0 4000))
    light))



(defn- add-ground!
  "Add a plane as a ground, or floor, or whatever…"
  [scene w d]
  (let [plane (js/THREE.Mesh.
               (js/THREE.PlaneGeometry. w d)
               (js/THREE.MeshLambertMaterial. #js {:color 0xFFFFFF
                                                   :side js/THREE.BackSide}))]
    (set! (-> plane .-rotation .-x) (/ Math.PI 2))
    (set! (-> plane .-position .-x) (/ w 2))
    (set! (-> plane .-position .-z) (/ d 2))
    (set! (.-receiveShadow plane) true)
    (.add scene plane)))

;; ------------------------------


;; Warning ! Big bad function comming!

(defn view
  "Create a 3D view bound to the given canvas, according to options"
  [opts]
  (let [{:keys [canvas stats
                width height depth
                amount neighbours
                min-distance max-speed]} opts]  ;; Extract options
    (let [state (atom {:run false})  ;; simulation state
          w (.-width canvas)
          h (.-height canvas)
          camera (js/THREE.PerspectiveCamera. 75 (/ w h) 0.1 100000)
          scene (js/THREE.Scene.)
          controls (js/THREE.OrbitControls. camera)
          renderer (js/THREE.WebGLRenderer. #js {:antialias true
                                                 :canvas canvas})
          container (container3 width height depth)  ;; a box where we can put boids
          boids (boids/generate amount)  ;; generate n boids
          render! (fn render! []     ;; render function
                    (when (:run @state)
                      ;; if it's running, reschedule next  frame
                      (.requestAnimationFrame js/window render!))  
                    
                    (when-not (nil? stats)
                      (.begin stats))
                    
                    (.render renderer scene camera)

                    (logic/update-boids! boids width height depth
                                         opts)
                    
                    (when-not (nil? stats)
                      (.end stats)))
          ]
      (.setSize renderer w h)  ;; scale to screen
      (set! (-> renderer .-shadowMap .-enabled) true)  ;; enable shadows
      (.setClearColor renderer 0xf0f0f0)  ;; background color
      (.set (.-position camera) 5000 5000 5000)

      (let [center (.clone (.-position container))]
        ;; set the orbiting center to the container's center
        (set! (.-target controls) center)        
        ;; make the camera look at center
        (.lookAt camera center))

      ;; add objects to the scene
      (draw-axes! scene)
      (.add scene (light))
      (add-ground! scene width depth)
      (.add scene container)

      (doseq [item boids] ;; add each boids
        (.copy (.-position item)   ;; at a random start position
               (maths/random-position width height depth))
        (.add container item))


      ;; START
      (render!)

      
      ;; return callbacks
      {:stats stats
       :start (fn []
                (when (false? (:run @state))
                  (swap! state assoc :run true)
                  (render!))
                nil)
       :stop (fn []
               (swap! state assoc :run false)
               nil)})))




