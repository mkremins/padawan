(defproject padawan "0.1.0"
  :description "Multiplayer Clojure web REPL for teaching and learning"
  :url "http://github.com/mkremins/padawan"

  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [org.clojure/data.json "0.2.4"]
   [compojure "1.1.6"]
   [http-kit "2.1.18"]
   [ring/ring-core "1.2.2"]]

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]}}

  :main padawan.core)