(ns swarm.tools
  "Utils like loggers, formaters & stuffs"
  (:refer-clojure :exclude [print]))

(def enabled (atom false))


(defn enable-logging!
  ([]
   (reset! enabled true))
  ([state]
   (reset! enabled (boolean state))))


(defn disable-logging!
  []
  (enable-logging! false))



(defn- args->js
  "Convert args to a printable javascript item or array"
  [coll]
  (clj->js
   (cond
    (not (seq? coll)) coll
    (empty? coll) nil
    (nil? (first (rest coll))) (first coll)
    :else coll)))



(def levels {:log  "log"
             :warn "warn"
             :err  "error"})

;; ------------------------------------


(defn print
  "Print with a particular loglevel."
  ([value]
   (print :log value))
  ([loglevel value]
   (let [value (args->js value)]
     (case loglevel   ;; Must stay like that as javascipt is "sometime" shitty. Refactor it if you dare!
       :warn (.warn js/console value)
       :err (.error js/console value)
       ;else
       (.log js/console value)))))



(defn stats
  "Create a stats.js instance"
  []
  (let [stats (js/Stats.)
        style (-> stats .-domElement .-style)]
    (set! (-> style .-position) "absolute")
    (set! (-> style .-left) "0px")
    (set! (-> style .-right) "0px")
    stats))


(defn vector3
  ([]
   (vector3 0 0 0))
  ([x y z]
   (js/THREE.Vector3. x y z)))








