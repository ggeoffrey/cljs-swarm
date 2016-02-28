;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;|
;;                                                                   |
;;  Hi visitor ! Glad to see you here !                             |
;;  Ready to eat some brackets ?                                     |
;;                                                                   |
;;  This is serious ! So first, please have                          |
;;  a pony :                                                         |
;;                                      __         __----__          |
;;                                     /  \__..--'' `-__-__''-_      |
;;                                    ( /  \    ``--,,  `-.''''`     |
;;                                    | |   `-..__  .,\    `.        |
;;                      ___           ( '.  \ ____`\ )`-_    `.      |
;;               ___   (   `.         '\   __/   __\' / `:-.._ \     |
;;              (   `-. `.   `.       .|\_  (   / .-| |'.|    ``'    |
;;               `-.   `-.`.   `.     |' ( ,'\ ( (WW| \W)j           |
;;       ..---'''':-`.    `.\   _\   .||  ',  \_\_`/   ``-.          |
;;     ,'      .'` .'_`-,   `  (  |  |''.   `.        \__/           |
;;    /   _  .'  :' ( ```    __ \  \ |   \ ._:7,______.-'            |
;;   | .-'/  : .'  .-`-._   (  `.\  '':   `-\    /                   |
;;   '`  /  :' : .: .-''>`-. `-. `   | '.    |  (                    |
;;      -  .' :' : /   / _( `_: `_:. `.  `;.  \  \                   |
;;      |  | .' : /|  | (___(   (      \   )\  ;  |                  |
;;     .' .' | | | `. |   \\\`---:.__-'') /  )/   |                  |
;;     |  |  | | |  | |   ///           |/   '    |                  |
;;    .' .'  '.'.`; |/ \  /     /             \__/                   |
;;    |  |    | | |.|   |      /-,_______\       \                   |
;;   /  / )   | | '|' _/      /     |    |\       \                  |
;; .:.-' .'  .' |   )/       /     |     | `--,    \                 |
;;      /    |  |  / |      |      |     |   /      )                |
;; .__.'    /`  :|/_/|      |      |      | (       |                |
;; `-.___.-`;  / '   |      |      |      |  \      |                |
;;        .:_-'      |       \     |       \  `.___/                 |
;;                    \_______)     \_______)                        |
;;                                                                   |
;;                                                                   |
;;  Awesome ! Now we can start playing                               |
;;  with the most powerfull language                                 |
;;  mankind ever invented !                                          |
;;                                                                   |
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;|


(ns ^:figwheel-always
  swarm.core
  "Entry point of this program, see (main) "
  (:require [swarm.tools :as t :refer [print]]
            [swarm.view :refer [view]])
  (:refer-clojure :exclude [print]))


(def default-options
  "Will be overwritten by user's options"
  {:width 6000
   :height 6000
   :depth 6000
   :amount 120
   :neighbours 5
   :min-distance 75
   :max-speed 12
   :dev-mode true})


(defn on-js-reload
  "Called by fighweel after hot code push"
  []
  ;; doing nothing.
  )


;; TODO: check and clean parametres
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
    (when (:dev-mode options)
      (t/enable-logging!))       ;; enable console output

    ;;return what the view returns as a JS object
    (clj->js
     (view options))))
