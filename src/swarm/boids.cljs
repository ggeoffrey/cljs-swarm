(ns swarm.boids
  "Everything to build boids. Inside a small and fancy pocket!"
  (:require [swarm.tools :as t]))


(defn- ->geometry3
  "Instanciate a new default geometry"
  []
  (js/THREE.SphereGeometry. 10 10 10))


(def geometry3
  "Cached ->geometry. Singleton for awesome performances. Yeah !
  One geometry to shape them all, and in the darkness bind them."
  (memoize ->geometry3))

(defn- ->material3
  "Instantiate a new default material"
  []
  (js/THREE.MeshBasicMaterial. #js {:color 0x0000ff}))

(def material3
  "Cached ->material. Almost 20% cooler."
  (memoize ->material3))


(defn- boid3 
  "Create a boid as a THREE.Mesh instance"
  []
  (let [boid (js/THREE.Mesh.
              (geometry3)
              (material3))]
    (set! (-> boid .-direction) (t/vector3 1 1 -1))
    (set! (.-castShadow boid) true)
    boid))


(defn generate
  "Generate n boids"
  [n]
  (take n (repeatedly boid3)))
