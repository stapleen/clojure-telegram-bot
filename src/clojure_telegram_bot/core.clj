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

(defn echo
  ""
[bot update]
(let [chat-id (get-in update [:message :chat :id])
      reply-to (get-in update [:message :message-id])
      text (get-in update [:message :text])]
  (defn fetch-url [url]
    (html/html-resource (java.net.URL. url)))
  (def html (fetch-url "https://hh.ru/search/vacancy?schedule=remote&clusters=true&area=1&no_magic=true&enable_snippets=true&salary=&st=searchVacancy&fromSearch=true&text=Frontend+%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%87%D0%B8%D0%BA&from=suggest_post"))

  (def div-main-content (html/select html [:html :body :div.HH-Supernova-MainContent :div.main-content-wrapper :div.main-content]))
  (def div-column-container (html/select div-main-content [:div :div.bloko-columns-wrapper :div.bloko-column_container]))
  (def hh-content (html/select div-column-container [:div.bloko-column_l-16 :div.sticky-container :div.HH-StickyParentAreaResizer-Content]))
  (def vacancy-results (html/select hh-content [:div.HH-SearchVacancyDropClusters-XsHiddenOnClustersOpenItem :div.bloko-column_xs-4 :div.bloko-gap_l-top :div :div.vacancy-serp]))
  (println "html" (html/select vacancy-results [:div.vacancy-serp-item]))
  ;; (cg/send-message bot chat-id result))
    ))


(defn -main
  "main function"
  [& _]
  (println ">>> launching application...")
  (cg/poll-updates bot interval echo))