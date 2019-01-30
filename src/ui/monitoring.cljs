(ns ui.monitoring
  (:require [re-frame.core :refer [reg-sub]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(reg-sub
  :monitoring
  (fn-traced [db _]
             (-> db :remote-db :monitoring)))

(reg-sub
  :monitoring/pinsize
  :<- [:monitoring]
  (fn-traced [monitoring]
             (:pinsize monitoring)))

(reg-sub
  :monitoring/freespace
  :<- [:monitoring]
  (fn-traced [monitoring]
             (:freespace monitoring)))
