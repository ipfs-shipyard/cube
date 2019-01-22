(ns shared.setup)

(def setup-steps [:welcome
                  :choose-hosting
                  :create-groups
                  :create-users
                  :done])

(defn get-next-step [step]
  (let [index (.indexOf setup-steps step)]
    (get setup-steps (+ index 1))))
