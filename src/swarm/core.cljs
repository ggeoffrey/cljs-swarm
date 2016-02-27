;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Hi visitor ! Glade to see you here !
;;  Ready to eat some brackets ?
;;
;;  This is serious ! So first, please have
;;  a pony :
;;
;;      ,;;*;;;;,
;;     .-'``;-');;.
;;    /'  .-.  /*;;
;;  .'    \d    \;;               .;;;,
;; / o      `    \;    ,__.     ,;*;;;*;,
;; \__, _.__,'   \_.-') __)--.;;;;;*;;;;,
;;  `""`;;;\       /-')_) __)  `\' ';;;;;;
;;     ;*;;;        -') `)_)  |\ |  ;;;;*;
;;     ;;;;|        `---`    O | | ;;*;;;
;;     *;*;\|                 O  / ;;;;;*
;;    ;;;;;/|    .-------\      / ;*;;;;;
;;   ;;;*;/ \    |        '.   (`. ;;;*;;;
;;   ;;;;;'. ;   |          )   \ | ;;;;;;
;;   ,;*;;;;\/   |.        /   /` | ';;;*;
;;    ;;;;;;/    |/       /   /__/   ';;;
;;    '*jgs/     |       /    |      ;*;
;;         `""""`        `""""`     ;'
;;
;;
;;
;; Awesome ! Now we can start playing
;; with the most powerfull language
;; mankind ever invented !
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns ^:figwheel-always
  swarm.core
  "Entry point of this program, see (main) "
  (:require [swarm.tools :as t :refer [print]]
            [swarm.view :refer [view]])
  (:refer-clojure :exclude [print]))


(def default-options {:width 6000
                      :height 6000
                      :depth 6000
                      :amount 60
                      :neighbours 5
                      :min-distance 25
                      :max-speed 10
                      :dev-mode false})



(defn on-js-reload
  "Called by fighweel after hot code push"
  []
  ;; doing nothing ATM.
  )


;; Not finished yet
;; TODO
(defn- build-options
  "Merge user options with default ones, clean and check."
  [user-opts]
  (let [user-opts (js->clj user-opts :keywordize-keys true)
        stats (t/stats)]
    (merge default-options user-opts {:stats stats})))


;; -----------------------------------------

(defn ^:export  main
  "Entry point, unmangled name"
  [options]
  (let [options (build-options options)]   ;; get clean parameters
    (t/enable-logging! (:dev-mode options))  ;; enable dev-mode accordingly
    (enable-console-print!)                  ;; same here
    ;;return what the view returns as a JS object
    (clj->js
     (view options))))
