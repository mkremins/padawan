(defproject padawan "0.1.0"
  :description "Multiplayer Clojure web REPL for teaching and learning"
  :url "http://github.com/mkremins/padawan"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"
            :distribution :repo}

  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [org.clojure/data.json "0.2.4"]
   [compojure "1.1.6"]
   [http-kit "2.1.18"]
   [javax.servlet/servlet-api "2.5"]
   [ring/ring-core "1.2.2"]]

  :main padawan.core)
