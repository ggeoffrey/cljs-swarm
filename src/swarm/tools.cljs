(ns swarm.tools
  "Utils like loggers, formaters & stuffs."
  (:refer-clojure :exclude [print]))


(def enabled (atom false))

(defn enable-logging!
  "Allow logging to console."
  ([]
   (enable-console-print!) ;; never disabled but not really important
   (enable-logging! true))
  ([state]
   (reset! enabled (boolean state))))


(defn- args->js
  "Convert args to a printable javascript item or array."
  [coll]
  (clj->js
   (cond
    (not (seq? coll)) coll
    (empty? coll) nil
    (nil? (first (rest coll))) (first coll)
    :else coll)))

(defn- ref-to-native
  "Give a reference to a native function
  while preserving the original binding."
  [native-object function]
  (.bind  (aget native-object function) native-object))


(def levels
  "Native logging functions bound to keywords."
  (let [levels (list "warn" "error" "info") ;; native levels
        ;; we want a map like {:warn js/console.warn}
        map (into {}   ;; put tuples into a map
                  ;; generate tuples like [:warn console.warn]
                  (mapv (fn [name] [(keyword name)
                                   (ref-to-native js/console name)])
                        levels))]
    ;; keep the original (print) function for default logging
    (merge map {:log cljs.core/print})))



;; ------------------------------------


(defn print
  "Print with a particular loglevel."
  ([value]
   (print :log value))
  ([loglevel value]
   (when @enabled
     ;; extract and execute
     ((get levels loglevel)
      ;; with correct parameters
      (cond 
       (= :log loglevel) value
       :else (args->js value))))))


(defn stats
  "Create a Stats.js instance."
  []
  (let [stats (js/Stats.)
        style (-> stats .-domElement .-style)]
    (set! (-> style .-position) "absolute")
    (set! (-> style .-left) "0px")
    (set! (-> style .-right) "0px")
    stats))


