(ns padawan.core
  (:use [compojure.core]
        [org.httpkit.server])
  (:require [clojail.core :refer [sandbox]]
            [clojail.testers :refer [blanket secure-tester-without-def]]
            [clojure.data.json :as json]
            [compojure.handler :as handler]
            [compojure.route :as route])
  (:import java.io.StringWriter))

(def clients (atom {}))

(def sandboxed-eval
  (sandbox (conj secure-tester-without-def
                 (blanket "padawan"))))

(defn eval-code! [code]
  (try
    (let [form (binding [*read-eval* false] (read-string code))
          [value out] (with-open [out (StringWriter.)]
                        (let [value (sandboxed-eval form {#'*out* out})]
                          [value (str out)]))]
      {:code code :successful? true :value value :out out})
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

(defn -main [port & args]
  (let [port (Integer/parseInt port)]
    (println "Starting server on port" port "...")
    (run-server (handler/site app-routes) {:port port})
    (println "Server started.")))
