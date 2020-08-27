(ns clojure-telegram-bot.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as chsr]
            [clojure.string :as str]
            [meinside.clogram :as cg]
            [net.cgrand.enlive-html :as html]))

(def token "1389261646:AAFb5UMGkAsgu2G8pXeqbRD3sMlf64TTYQM")
(def interval 1)
(def verbose? true)
(def bot (cg/new-bot token :verbose? verbose?))

(defn html-parsing
  ""
  [bot update]
  (let [chat-id (get-in update [:message :chat :id])
        reply-to (get-in update [:message :message-id])
        text (get-in update [:message :text])]

    (defn fetch-url [url]
      (html/html-resource (java.net.URL. url)))

    (def html (fetch-url "https://hh.ru/search/vacancy?schedule=remote&clusters=true&area=1&no_magic=true&enable_snippets=true&salary=&st=searchVacancy&fromSearch=true&text=Frontend+%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%87%D0%B8%D0%BA&from=suggest_post"))
    (def links (html/select html [:div.vacancy-serp-item__info :a]))
    (def vacancy-url (map #(get-in % [:attrs :href]) (html/select html [:div.vacancy-serp-item__info :a])))

    (cg/send-message bot chat-id (nth vacancy-url 0))))

(defn -main
  "main function"
  [& _]
  (println ">>> launching application...")
  (cg/poll-updates bot interval html-parsing))