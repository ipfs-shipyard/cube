(ns cube.permissions
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(s/def ::title string?)
(s/def ::description string?)

(s/def ::permission (s/keys :req-un [::title
                                     ::description]))

(s/def ::permissions (s/coll-of ::permission))

(s/def ::group (s/keys :req-un [::title
                               ::description
                               ::permissions]))

(s/def ::user (s/keys :req-un [::username
                               ::password
                               ::role]))
