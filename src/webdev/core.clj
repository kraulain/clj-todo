(ns webdev.core
  (:require [ring.adapter.jetty :as jetty]))

(defn greet [req]
  (case (:uri req)
    "/"     {:Status 200 :body "Hello, World!" :headers {}}
    "/ping" {:Status 200 :body "Ping ping" :headers {}}
            {:Status 404 :body "Not Found" :headers {}}))

(defn -main [port]
  (jetty/run-jetty greet
                   {:port (Integer. port)}))
