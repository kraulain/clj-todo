(ns webdev.core
  (:require [webdev.item.model :as items]
            [webdev.item.handler :refer [handle-index-items
                                         handle-create-item
                                         handle-delete-item
                                         handle-update-item]])
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [compojure.core :refer [defroutes ANY GET POST PUT DELETE]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]))

(def db "jdbc:postgresql://localhost/webdev")

(defn greet [req]
  {:Status 200
   :body "Hello, World!"
   :headers {}})

(defn goodbye [req]
  {:Status 200
   :body "Goodbye, Cruel World!"
   :headers {}})

(defn about [req]
  {:Status 200
   :body "Hi, I am kraulain!"
   :headers {}})

(defn yo [req]
  (let [name (:name (:route-params req))]
    {:status 200
     :body (str "Yo! " name "!")
     :headers {}}))

(def ops
  {"+" +
   "-" -
   "*" *
   ":" /})

(defn calc [req]
  (let [params (:route-params req)
        a (Integer. (:a params))
        b (Integer. (:b params))
        op (:op params)
        f (get ops op)]
    (if f
      {:status 200
       :body (str (f a b))
       :headers {}}
      {:status 404
       :body (str "Unknown operator: " op)
       :headers {}})))

(defroutes routes
  (GET "/" [] greet)
  (GET "/goodbye" [] goodbye)
  (GET "/about" [] about)
  (ANY "/request" [] handle-dump)
  (GET "/yo/:name" [] yo)
  (GET "/calc/:a/:op/:b" [] calc)

  (GET "/items" [] handle-index-items)
  (POST "/items" [] handle-create-item)
  (PUT "/items/:item-id" [] handle-update-item)
  (DELETE "/items/:item-id" [] handle-delete-item)
  (not-found "Page not found."))

(defn wrap-db [hdlr]
  (fn [req]
    (hdlr (assoc req :webdev/db db))))

(defn wrap-server [hdlr]
  (fn [req]
    (assoc-in (hdlr req) [:header "Server"] "Simple todo server")))

(def sim-methods {"PUT" :put
                   "DELETE" :delete})

(defn wrap-simulated-methods [hdlr]
  (fn [req]
    (if-let [method (and (= :post (:request-method req))
                         (sim-methods (get-in req [:params "_method"])))]
      (hdlr (assoc req :request-method method))
      (hdlr req))))

(def app
  (wrap-server
   (wrap-file-info
    (wrap-resource
     (wrap-db
      (wrap-params
       (wrap-simulated-methods
        routes)))
     "static"))))

(defn -main [port]
  (items/create-table db)
  (jetty/run-jetty app
                   {:port (Integer. port)}))

(defn -dev-main [port]
  (items/create-table db)
  (jetty/run-jetty (wrap-reload #'app)
                   {:port (Integer. port)}))
