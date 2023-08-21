(ns webdev.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn greet [req]
  (case (:uri req)
    "/"     {:Status 200 :body "Hello, World!" :headers {}}
    "/ping" {:Status 200 :body "Ping ping" :headers {}}
            {:Status 404 :body "Page Not Found" :headers {}}))

(defn -main [port]
  (jetty/run-jetty greet
                   {:port (Integer. port)}))

(defn -dev-main [port]
  (jetty/run-jetty (wrap-reload #'greet)
                   {:port (Integer. port)}))
