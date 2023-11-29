(ns stoclj.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [stoclj.brapi :as brapi]
            [stoclj.utilities :refer [stringify-json get-type]]
            [stoclj.db :as db]))

(def token (System/getenv "BRAPI_API_TOKEN"))


(defn get-all-stocks []
  (let [raw-data
        (brapi/get-all-stocks token :name :stock)

        brapi-data
        (map (fn [x] {:nome (:name x) :codigo (:stock x)}) raw-data)]
    brapi-data))


(defn get-stock [id]
  (let [raw-data   (brapi/get-stock id token)
        brapi-data {:nome                             (:longName raw-data)
                    :codigo                           (:symbol raw-data)
                    :tipo_acao                        (get-type (:symbol raw-data))
                    :variacao_preco_diario            (:regularMarketChange raw-data)
                    :variacao_preco_diario_percentual (:regularMarketChangePercent raw-data)
                    :dh_ultimo_preco                  (:regularMarketTime raw-data)
                    :ultimo_preco                     (:regularMarketPrice raw-data)
                    :preco_maximo                     (:regularMarketDayHigh raw-data)
                    :preco_minimo                     (:regularMarketDayLow raw-data)
                    :preco_ultimo_fechamento          (:regularMarketPreviousClose raw-data)
                    :preco_abertura                   (:regularMarketOpen raw-data)
                    :descricao                        (:longBusinessSummary
                                                       (:summaryProfile raw-data))}]
    brapi-data))


(defn get-balance [field]
  (reduce + (map (fn [x]
                   (let [value (field x)]
                     (if (= :venda (:tipo x))
                       (- value)
                       value)))
                 (db/history))))


(defn start-transaction [response-body transaction-type]
  (let [{:keys [ticker qte]}   response-body
        {:keys [ultimo_preco]} (get-stock ticker)] 
    
    (letfn [(now [] (java.util.Date.))]
      (db/register {:qte            qte
                    :tipo           (keyword transaction-type)
                    :preco_unitario ultimo_preco
                    :preco_total    (* qte ultimo_preco)
                    :dh             (now)})))
  
  (if (neg? (get-balance :qte))
    (do (db/undo-register) {:status 403
                            :body (stringify-json {:msg "Você não possui ações suficientes."})})
    {:status 200 
     :body (stringify-json {:msg "Transação realizada com sucesso."})}))


(defn in-order [coll order-by desc]
  (let [sorted-coll (if order-by
                      (sort-by (keyword order-by) coll)
                      coll)]
    (if desc
      (reverse sorted-coll)
      sorted-coll)))


(defroutes app-routes
  (GET "/" [] "Olá mundo")

  (context "/api/stocks" [] 
    (GET "/" [] {:headers {"Content-Type" "application/json"}
                 :body    (stringify-json (get-all-stocks))})
    
    (GET "/:id" [id] {:headers {"Content-Type" "application/json"} 
                      :body    (stringify-json (get-stock id))}))
  
  (context "/api/carteira" []
    (GET "/extrato" request {:headers {"Content-Type" "application/json"}
                             :body    (stringify-json 
                                       {:saldo (get-balance :preco_total)
                                        :transacoes (in-order (db/history)
                                                              (:por (:params request))
                                                              (= "true" (:desc (:params request))))})})
    
    (POST "/compra"
      request 
      (merge 
       (start-transaction (:body request) :compra) 
       {:headers {"Content-Type" "application/json"}}))

    (POST "/venda"
      request
      (merge
       (start-transaction (:body request) :venda) 
       {:headers {"Content-Type" "application/json"}})) )

  (route/not-found "Não encontrado"))


(def app
  (wrap-defaults (wrap-json-body app-routes {:keywords? true}) api-defaults))