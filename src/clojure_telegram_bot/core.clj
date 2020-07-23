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
  (def goog-news (fetch-url "https://example.com"))
  (def html (nth goog-news 1))
  (def result (html/text (nth (html/select html [:html :body :div :h1]) 0)))
  (cg/send-message bot chat-id result))
  )

(defn -main
  "main function"
  [& _]
  (println ">>> launching application...")
  (cg/poll-updates bot interval echo))