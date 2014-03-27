(ns padawan.core
  (:use [compojure.core]
        [org.httpkit.server])
  (:require [clojure.data.json :as json]
            [compojure.handler :as handler]
            [compojure.route :as route]))

(def clients (atom {}))

(defn eval-code! [code]
  (try
    (let [value (eval (read-string code))]
      {:code code :successful? true :value value})
    (catch Exception error
      {:code code :successful? false :error (str error)})))

(defn handle-message [chan data]
  (let [sender (get-in @clients [chan :name])
        {:keys [op] :as message} (json/read-str data :key-fn keyword)]
    (when (= op "eval")
      (let [result (merge (eval-code! (:code message)) {:sender sender})]
        (doseq [client (keys @clients)]
          (send! client
                 (json/write-str result
                                 :key-fn name
                                 :value-fn #(if (var? %2)
                                                (:name (meta %2))
                                                %2))))))))

(defn handle-connect [req]
  (println "Handle connect: " (pr-str req))
  (with-channel req chan
    (swap! clients assoc chan {:name (if (empty? @clients) "p1" "p2")})
    (on-receive chan (partial handle-message chan))
    (on-close chan #(swap! clients dissoc chan))))

(defroutes app-routes
  (GET "/ws" [] handle-connect)
  (route/files "" {:root "resources"})
  (route/not-found "<p>Page not found.</p>"))

(defn -main [& args]
  (let [port 8000]
    (println "Starting server on port" port "...")
    (run-server (handler/site app-routes) {:port port})
    (println "Server started.")))
