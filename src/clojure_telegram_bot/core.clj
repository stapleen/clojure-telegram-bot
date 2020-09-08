(ns clojure-telegram-bot.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as chsr]
            [clojure.string :as str]
            [meinside.clogram :as cg]
            [net.cgrand.enlive-html :as html]
            [comb.template :as template]
            [clojure.java.jdbc :as jdbc]
            [clojure-telegram-bot.config :as config]))

(def token config/token)
(def interval config/interval)
(def verbose? config/verbose?)
(def bot (cg/new-bot token :verbose? verbose?))

(def db config/db-config)

(def url-vacancy-min-length 0)
(def url-vacancy-max-length 30)

(def info "Список доступных команд:\n/set - отправить ссылку на поиск вакансий\n/update - обновить список вакансий\n/get - получить новые вакансии\n/cancel - сбросить просмотренные вакансии")

(defn do-short-links
  "shorten links length"
  [vacancies-url]
  (mapv #(subs % url-vacancy-min-length url-vacancy-max-length) vacancies-url))

(defn insert-vacancies-url
  "insert urls in db"
  [vacancies-url chat-id user-id]
  (def url-list (do-short-links vacancies-url))

  (def urls-apostrophe (mapv #(template/eval "('<%= url %>', <%= id %>)" {:url %, :id user-id}) url-list))
  (def urls-for-insert (str/join "," urls-apostrophe))
  (def query (template/eval "INSERT IGNORE INTO vacancies (vacancy_url, user_id) VALUES <%= params %>" {:params urls-for-insert}))

  (jdbc/execute! db query)
  (cg/send-message bot chat-id "Список вакансий сформирован"))

(defn fetch-url [url] (html/html-resource (java.net.URL. url)))

(defn html-parsing
  "function for parsing html"
  [chat-id]
  (def data-in-db (jdbc/query db ["SELECT id, url FROM users WHERE chat_id = ?" chat-id]))

  (if (= data-in-db ())
    (do (cg/send-message bot chat-id "Сначала нужно добавить ссылку"))
    (do
      (def url-hh (get-in (nth data-in-db 0) [:url]))
      (def user-id (get-in (nth data-in-db 0) [:id]))

      (def html (fetch-url url-hh))
      (def links (html/select html [:div.vacancy-serp-item__info :a]))
      (def vacancies-url (mapv #(get-in % [:attrs :href]) links))
      (insert-vacancies-url vacancies-url chat-id user-id))
    ))

(defn mark-vacancy-viewed
  "mark vacancy as view in the database"
  [id]
  (jdbc/update! db :vacancies {:is_show 1} ["id = ?" id]))

(defn send-vacancies
  "send list of vacancies"
  [chat-id]
  (def user-data-in-db (jdbc/query db ["SELECT id FROM users WHERE chat_id = ?" chat-id]))

  (if (= user-data-in-db ())
    (do (cg/send-message bot chat-id "Сначала нужно добавить ссылку"))
    (do
      (def user-data-id (get-in (nth user-data-in-db 0) [:id]))
      (def vacancy-list (jdbc/query db ["SELECT id, vacancy_url FROM vacancies WHERE is_show = ? AND user_id = ?" 0 user-data-id]))
      (if (= vacancy-list [])
        (do (cg/send-message bot chat-id "Все вакансии просмотрены"))
        (do
          (def vacancy-urls-list (mapv #(get-in % [:vacancy_url]) vacancy-list))
          (def id-url-for-send (mapv #(get-in % [:id]) vacancy-list))
          (mapv #(mark-vacancy-viewed %) id-url-for-send)
          (def urls-for-send (str/join "\n" vacancy-urls-list))
          (cg/send-message bot chat-id urls-for-send))))))

(defn reset-viewed-vacancy
  "reset viewed vacancies"
  [chat-id]
  (def data-db (jdbc/query db ["SELECT id FROM users WHERE chat_id = ?" chat-id]))

  (if (= data-db ())
    (do (cg/send-message bot chat-id "Сначала нужно добавить ссылку"))
    (do
      (def user-id-db (get-in (nth data-in-db 0) [:id]))
      (jdbc/update! db :vacancies {:is_show 0} ["is_show = ? AND user_id = ?" 1 user-id-db])
      (cg/send-message bot chat-id "Список просмотренных вакансий сброшен"))))

(defn set-url
  "save url in db"
  [chat-id user-message]
  (def user-text (str/trim (nth user-message 1)))
  (def url-for-parsing-in-db (jdbc/query db ["SELECT url FROM users WHERE chat_id = ?" chat-id]))

  (if (= url-for-parsing-in-db ())
    (do (jdbc/execute! db ["INSERT INTO users (chat_id, url) VALUES(?, ?)" chat-id user-text]))
    (do (jdbc/update! db :users {:url user-text} ["chat_id = ?" chat-id])))
  (cg/send-message bot chat-id "Ссылка добавлена"))

(defn bot-response
  "bot response"
  [bot update]
  (let [chat-id (get-in update [:message :chat :id])
        reply-to (get-in update [:message :message-id])
        text (get-in update [:message :text])]

    (def user-message (str/split text  #" "))
    (def command (nth user-message 0))

    (cond
      (= command "/set") (set-url chat-id user-message)
      (= command "/update") (html-parsing chat-id)
      (= command "/get") (send-vacancies chat-id)
      (= command "/cancel") (reset-viewed-vacancy chat-id)
      :else (cg/send-message bot chat-id info))))

(defn -main
  "main function"
  [& _]
  (println ">>> launching application...")

  (cg/poll-updates bot interval bot-response))