--liquibase formatted sql

--changeset shyam.akirala:1 runOnChange:false

CREATE TABLE IF NOT EXISTS `StateMachines` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(1000) NOT NULL,
  `version` SMALLINT UNSIGNED NOT NULL,
  `description` VARCHAR(10) DEFAULT NULL,
  `correlationId` VARCHAR(128) DEFAULT NULL,
  `createdAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updatedAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_correlationId` (`correlationId`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table StateMachines;
