(ns swarm.view.boids
  (:require [swarm.tools]))


(defn- get-default-geometry
  "Generate a common geometry for the boids (they are all the same)"
  []
  (new js/THREE.SphereGeometry 5 5 5))

(def get-geometry (memoize get-default-geometry))


(defn- get-default-material
  "Generate a common geometry for the boids (they are all the same)"
  []
  (new js/THREE.MeshBasicMaterial #js {"color" 0x0000ff}))

(def get-material (memoize get-default-material))


(defn create 
  "Create a boid. Formerly a Three.Mesh"
  []
  (let [geometry (get-geometry)
        material (get-material)
        boid (new js/THREE.Mesh geometry material)
        direction (new js/THREE.Vector3)]
    (.set direction 0 0 0)
    (set! (.-direction boid) direction)
    ;(set! (.-velocity boid) 1)
    boid))




(defn generate 
  "Generate n boids"
  [n]
  (doall (map #(create) (range n))))