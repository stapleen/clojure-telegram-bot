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

(defn do-short-links
  "shorten links length"
  [vacancies-url]
  (mapv #(subs % url-vacancy-min-length url-vacancy-max-length) vacancies-url))

(defn insert-vacancies-url
  "insert urls in db"
  [vacancies-url chat-id]
  (def url-list (do-short-links vacancies-url))

  (jdbc/insert-multi! db :vacancies [:vacancy_url] (mapv #(vector %) url-list))
  (cg/send-message bot chat-id "Список вакансий сформирован"))

(defn html-parsing
  "function for parsing html"
  [chat-id]
  (defn fetch-url [url] (html/html-resource (java.net.URL. url)))

  (def html (fetch-url url-hh))
  (def links (html/select html [:div.vacancy-serp-item__info :a]))
  (def vacancies-url (mapv #(get-in % [:attrs :href]) links))

  (insert-vacancies-url vacancies-url chat-id))

(defn mark-vacancy-viewed
  "mark vacancy as view in the database"
  [id]
  (jdbc/update! db :vacancies {:is_show 1} ["id = ?" id]))

(defn get-vacancies
  "send list of vacancies"
  [chat-id]
  (def vacancy-list (jdbc/query db ["SELECT id, vacancy_url FROM vacancies WHERE is_show = ?" 0]))
  ;; while one link
  (def url-for-send (get-in (nth vacancy-list 0) [:vacancy_url]))
  (def id-url (get-in (nth vacancy-list 0) [:id]))

  (mark-vacancy-viewed id-url)
  (cg/send-message bot chat-id url-for-send))

(defn bot-response
  "bot response"
  [bot update]
  (let [chat-id (get-in update [:message :chat :id])
        reply-to (get-in update [:message :message-id])
        text (get-in update [:message :text])]

    (cond
      (= text "/new") (html-parsing chat-id)
      (= text "/get") (get-vacancies chat-id)
      )))

(defn -main
  "main function"
  [& _]
  (println ">>> launching application...")

  (cg/poll-updates bot interval bot-response))