(ns swarm.tools
  "Contains:
  - log helpers
  - args verification
  - utils generation
  ")



(defn- get-args
  "Return the first arg or all the list as a js-obj"
  [collection]
  (if (= (count collection) 1)
    (clj->js (first collection))
    (clj->js collection)))


(defn log
  "Log in the console.
  If a collection of size 1 is given then only the first item will be printed.
  Else the collection will be printed as a JS array.
  "
  [& args]
  (.log js/console (get-args args)))

(defn warn
  "Warn in the console.
  If a collection of size 1 is given then only the first item will be printed.
  Else the collection will be printed as a JS array.
  "
  [& args]
  (.warn js/console (get-args args)))


(defn canvas?
  "Chetk if the given object is a DOM canvas"
  [canvas]
  (and
   (not (nil? canvas))
   (not (nil? (.-nodeName canvas)))
   (= "canvas" (.toLowerCase (.-nodeName canvas)))))


(defn create-stats
  "Create a Stat.js instance and set it to appear in the upper-left corner as a HUD."
  []
  (let [stats (new js/Stats)
        style (-> stats
                  (.-domElement)
                  (.-style))]
    (set! (.-position style) "absolute")
    (set! (.-left style) "0px")
    (set! (.-top style) "0px")
    stats))
