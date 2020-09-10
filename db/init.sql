CREATE DATABASE IF NOT EXISTS `clojure_bot_telegram`;
USE `clojure_bot_telegram`;

CREATE TABLE IF NOT EXISTS `users` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id пользователя',
  `chat_id` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'chat id пользователя',
  `url` varchar(350) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'ссылка с поиском вакансий',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='таблица с данными пользователя';

CREATE TABLE IF NOT EXISTS `vacancies` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id вакансии',
  `vacancy_url` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'ссылка на вакансию',
  `is_show` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'отображалась вакансия или нет',
  `user_id` int NOT NULL COMMENT 'id пользователя',
  PRIMARY KEY (`id`),
  UNIQUE KEY `vacancy_url_user_id` (`vacancy_url`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='таблица с вакансиями';