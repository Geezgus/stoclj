(ns stoclj.utilities
  (:require [clojure.string :as string]
            [cheshire.core :refer [parse-string generate-string]]))

(defn parse-json [json]
  (parse-string json true))


(defn stringify-json [hashmap]
  (generate-string hashmap {:pretty true}))


(defn get-fields [fields]
  (fn [a-map] (select-keys a-map fields)))

(defn get-type [sym]
  (let [patterns [["1" "Direito de Subscrição - Ação Ordinária"]
                  ["2" "Direito de Subscrição - Ação Preferencial"]
                  ["3" "Ordinária"]
                  ["4" "Preferencial"]
                  ["5" "Preferencial Classe A"]
                  ["6" "Preferencial Classe B"]
                  ["7" "Preferencial Classe C"]
                  ["8" "Preferencial Classe D"]
                  ["9" "Recibo de Subscrição - Ação Ordinária"]
                  ["10" "Recibo de Subscrição - Ação Preferencial"]
                  ["11" "BDRs/ETs/Units"]]]

    (second (last (filter #(string/ends-with? sym (first %)) patterns)))))