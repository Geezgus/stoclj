(ns stoclj.db)

(def transactions
  (atom []))

(defn history []
  @transactions)

(defn register [transaction]
  (let [coll (swap! transactions conj transaction)]
    (merge transaction {:id (count coll)})))

(defn undo-register []
  (swap! transactions pop))