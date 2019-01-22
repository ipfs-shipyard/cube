(ns shared.host
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(s/def ::not-empty-string (s/and string? (complement empty?)))
(s/def ::name ::not-empty-string)
(s/def ::desc ::not-empty-string)
(s/def ::enabled boolean?)
(s/def ::type #{:solo
                :aws
                :do
                :gcloud
                :custom})

(s/def ::host (s/keys :req [::name
                          ::desc
                          ::enabled
                          ::type]))

;; (s/valid? :shared.host/host {:shared.host/name "Solo"
;;                               :shared.host/type :aws
;;                               :shared.host/desc "AWS"
;;                               :shared.host/enabled true})
;; (s/explain :shared.host/host {:shared.host/name "Solo"
;;                               :shared.host/type :aws
;;                               :shared.host/desc "AWS"
;;                               :shared.host/enabled true})
;; (gen/sample (s/gen ::host))
