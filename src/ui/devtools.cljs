(ns ui.devtools
  (:require ;; TODO ruins dead code elimation
            [devtools.core :as devtools]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

;; TODO should only be in dev, ruins dead code elimation
(defn init! []
  ;; installs formatters for console.log when in browser js console
  (devtools/install! [:formatters :hints])
  ;; makes `println` output to browser js console
  (enable-console-print!))
