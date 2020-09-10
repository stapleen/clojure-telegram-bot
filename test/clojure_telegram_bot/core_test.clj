(ns clojure-telegram-bot.core-test
  (:require [clojure.test :refer :all]
            [net.cgrand.enlive-html :as html]
            [clojure-telegram-bot.core :refer :all]))

(deftest parse-html-test-first
  (testing "parse-html-test-first failed"
    (is (= "https://hh.ru/vacancy/38949794?query=Frontend%20%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%87%D0%B8%D0%BA" (-> "./test/clojure_telegram_bot/fixtures/index.html" java.io.File. html/html-resource parse-html first)))))

(deftest parse-html-test-last
  (testing "parse-html-test-last failed"
    (is (= "https://hh.ru/vacancy/39014335?query=Frontend%20%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%87%D0%B8%D0%BA" (-> "./test/clojure_telegram_bot/fixtures/index.html" java.io.File. html/html-resource parse-html last)))))

