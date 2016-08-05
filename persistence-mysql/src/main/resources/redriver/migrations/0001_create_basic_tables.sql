--liquibase formatted sql

--changeset yogesh.nachnani:1 runOnChange:false

CREATE TABLE `api` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dtype` varchar(31) not NULL,
  `body` longtext,
  `headers` longtext,
  `method` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

CREATE TABLE `schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `end_time` bigint(20) DEFAULT NULL,
  `repeat_interval` bigint(20) NOT NULL,
  `start_time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

CREATE TABLE `job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `side_lined` bit(1) NOT NULL,
  `sideline_reason` varchar(255) DEFAULT NULL,
  `api_id` bigint(20) NOT NULL,
  `schedule_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_atcl7ldp04r846fq0cep4e3wi` (`name`),
  KEY `FK_fwu7hgujr13bk0eea9jx6fyuk` (`api_id`),
  KEY `FK_kx009rgm477v7uc3cqsj240k5` (`schedule_id`),
  CONSTRAINT `FK_fwu7hgujr13bk0eea9jx6fyuk` FOREIGN KEY (`api_id`) REFERENCES `api` (`id`),
  CONSTRAINT `FK_kx009rgm477v7uc3cqsj240k5` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;