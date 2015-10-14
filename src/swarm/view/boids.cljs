(ns swarm.view.boids
  "All you need to create boids. 
  A boid is a basic THREE.Mesh with a 5*5*5 THREE.SphereGeometry and a blue THREE.MeshBasicMaterial."
  (:require [swarm.tools]))






(defn ^:private get-default-geometry
  "Generate a common geometry for the boids (they are all the same, so one object to shape them all)"
  []
  (new js/THREE.SphereGeometry 5 5 5))

;; Memoize the default geometry. We now have a singleton geometry.
(def get-geometry (memoize get-default-geometry))


(defn ^:private get-default-material
  "Generate a common material for the boids (they are all the same, so one object to paint them all)"
  []
  (new js/THREE.MeshBasicMaterial #js {"color" 0x0000ff}))

;; Memoize the default geometry. We now have a singleton material.
(def get-material (memoize get-default-material))


(defn create 
  "Create a boid. Formerly a Three.Mesh.
  They all share the same geometry and same material (same instance)."
  []
  (let [geometry (get-geometry)
        material (get-material)
        boid (new js/THREE.Mesh geometry material)]
    (set! (.-velocity boid) (new js/THREE.Vector3))
    boid))




(defn generate 
  "Generate n boids"
  [n]
  (take n (repeatedly create)))

