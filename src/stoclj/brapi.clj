(ns stoclj.brapi
  (:require [clj-http.client :as http]
            [stoclj.utilities :refer [parse-json get-fields]]))


(defn get-all-stocks [token & fields]
  (let [stocks
        (:stocks
         (parse-json
          (:body
           (http/get
            "https://brapi.dev/api/quote/list"
            {:accept       :json
             :query-params {"token"     token
                            "sortBy"    "name"
                            "sortOrder" "asc"}}))))]
    (if (nil? fields)
      stocks
      (map (get-fields fields) stocks))))


(defn get-stock [ticker token & fields]
  (let [stock
        (first
         (:results
          (parse-json
           (:body
            (http/get
             (str "https://brapi.dev/api/quote/" ticker)
             {:accept       :json
              :query-params {"token"   token
                             "modules" "summaryProfile"}})))))]
    (if (nil? fields)
      stock
      (select-keys stock fields))))
