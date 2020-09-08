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

(def db config/db-config)
(def info "Список доступных команд:\n/set - отправить ссылку на поиск вакансий\n/update - обновить список вакансий\n/get - получить новые вакансии\n/cancel - сбросить просмотренные вакансии")

(defn do-short-links
  [vacancies-url]
  (let [url-vacancy-min-length 0
        url-vacancy-max-length 30]
    (mapv #(subs % url-vacancy-min-length url-vacancy-max-length) vacancies-url)))

(defn insert-vacancies-url
  [vacancies-url chat-id user-id]
  (let [urls (do-short-links vacancies-url)
        urls-in-apostrophe (mapv #(template/eval "('<%= url %>', <%= id %>)" {:url %, :id user-id}) urls)
        urls-for-insert (str/join "," urls-in-apostrophe)
        query (template/eval "INSERT IGNORE INTO vacancies (vacancy_url, user_id) VALUES <%= params %>" {:params urls-for-insert})]
    (jdbc/execute! db query)
    (cg/send-message bot chat-id "Список вакансий сформирован")))

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))

(defn html-parsing
  [chat-id]
  (let [user-data (jdbc/query db ["SELECT id, url FROM users WHERE chat_id = ?" chat-id])]
   (if (= user-data ())
     (do (cg/send-message bot chat-id "Сначала нужно добавить ссылку"))
     (do
       (let [url-hh (get-in (nth user-data 0) [:url])
             id (get-in (nth user-data 0) [:id])
             html (fetch-url url-hh)
             links (html/select html [:div.vacancy-serp-item__info :a])
             vacancies-url (mapv #(get-in % [:attrs :href]) links)]
         (insert-vacancies-url vacancies-url chat-id id))))))

(defn mark-vacancy-viewed
  [id]
  (jdbc/update! db :vacancies {:is_show 1} ["id = ?" id]))

(defn send-vacancies
  [chat-id]
  (let [user-data (jdbc/query db ["SELECT id FROM users WHERE chat_id = ?" chat-id])]
    (if (= user-data ())
      (do (cg/send-message bot chat-id "Сначала нужно добавить ссылку"))
      (do
        (let [id (get-in (nth user-data 0) [:id])
              vacancies-data (jdbc/query db ["SELECT id, vacancy_url FROM vacancies WHERE is_show = ? AND user_id = ?" 0 id])]
          (if (= vacancies-data [])
            (do (cg/send-message bot chat-id "Все вакансии просмотрены"))
            (do
              (let [url-list (mapv #(get-in % [:vacancy_url]) vacancies-data)
                    id-list (mapv #(get-in % [:id]) vacancies-data)]
                (mapv #(mark-vacancy-viewed %) id-list)
                ;; переписать это
                (let [urls (str/join "\n" url-list)]
                (cg/send-message bot chat-id urls))))))))))

(defn reset-viewed-vacancy
  [chat-id]
  (let [user-data (jdbc/query db ["SELECT id FROM users WHERE chat_id = ?" chat-id])]

  (if (= user-data ())
    (do (cg/send-message bot chat-id "Сначала нужно добавить ссылку"))
    (do
      (let [id (get-in (nth user-data 0) [:id])]
        (jdbc/update! db :vacancies {:is_show 0} ["is_show = ? AND user_id = ?" 1 id])
        (cg/send-message bot chat-id "Список просмотренных вакансий сброшен"))))))

(defn set-url
  [chat-id user-message]
  (let [user-text (str/trim (nth user-message 1))
        url-in-db (jdbc/query db ["SELECT url FROM users WHERE chat_id = ?" chat-id])]

    (if (= url-in-db ())
      (do (jdbc/execute! db ["INSERT INTO users (chat_id, url) VALUES(?, ?)" chat-id user-text]))
      (do (jdbc/update! db :users {:url user-text} ["chat_id = ?" chat-id])))
    (cg/send-message bot chat-id "Ссылка добавлена")))

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

  (let [token config/token
        interval config/interval
        verbose? config/verbose?
        bot (cg/new-bot token :verbose? verbose?)]
        (cg/poll-updates bot interval bot-response)))