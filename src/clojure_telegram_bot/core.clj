(ns clojure-telegram-bot.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as chsr]
            [clojure.string :as str]
            [meinside.clogram :as cg]
            [net.cgrand.enlive-html :as html]
            [clojure.java.jdbc :as jdbc]
            [clojure-telegram-bot.config :as config]))

(def token config/token)
(def interval config/interval)
(def verbose? config/verbose?)
(def bot (cg/new-bot token :verbose? verbose?))

(def db config/db-config)

(def url-hh config/url)
(def url-vacancy-min-length 0)
(def url-vacancy-max-length 30)

(defn insert-vacancy-url
  "insert urls in db"
  [vacancy-url]
  (def url (subs (nth vacancy-url 0) url-vacancy-min-length url-vacancy-max-length))
  (jdbc/insert! db :vacancies {:vacancy_url url}))

(defn html-parsing
  "function for parsing html"
  []
  (defn fetch-url [url] (html/html-resource (java.net.URL. url)))
  (def html (fetch-url url-hh))

  (def links (html/select html [:div.vacancy-serp-item__info :a]))
  (def vacancy-url (map #(get-in % [:attrs :href]) (html/select html [:div.vacancy-serp-item__info :a])))

  (insert-vacancy-url vacancy-url))

(defn bot-response
  "bot response"
  [bot update]
  (let [chat-id (get-in update [:message :chat :id])
        reply-to (get-in update [:message :message-id])
        text (get-in update [:message :text])]

    (if (= text "/new")
      (do
        (html-parsing)))))

(defn -main
  "main function"
  [& _]
  (println ">>> launching application...")

  (cg/poll-updates bot interval bot-response))