(ns swarm.tools)

(defn- get-args 
  "Return the first arg or all the list as a js-obj"
  [coll]
  (if (= (count coll) 1)
    (clj->js (first coll))
   	(clj->js coll)))


(defn log 
  "Log in the console"
  [& args]
  (.log js/console (get-args args)))

(defn warn 
  "Warn in the console"
  [& args]
  (.warn js/console (get-args args)))


(defn canvas? 
  "Chetk if the given object is a dom canvas"
  [canvas]
  (and 
    (not (nil? canvas))
    (not (nil? (.-nodeName canvas)))
    (= "canvas" (.toLowerCase (.-nodeName canvas)))))


(defn create-stats 
  "Create a Stat.js instance"
  []
  (let [stats (new js/Stats)
    style (-> stats
      (.-domElement)
      (.-style))]
    (set! (.-position style) "absolute")
    (set! (.-left style) "0px")
    (set! (.-top style) "0px")
    stats))