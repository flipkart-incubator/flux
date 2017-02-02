--liquibase formatted sql

--changeset shyam.akirala:2 runOnChange:false

CREATE TABLE IF NOT EXISTS `States` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `version` SMALLINT UNSIGNED NOT NULL,
  `description` VARCHAR(10) DEFAULT NULL,
  `dependencies` VARCHAR(1000) DEFAULT NULL,
  `stateMachineId` BIGINT,
  `onEntryHook` varchar(500) DEFAULT NULL,
  `task` VARCHAR(1000) DEFAULT NULL,
  `onExitHook` varchar(500) DEFAULT NULL,
  `outputEvent` varchar(500) DEFAULT NULL,
  `retryCount` INT UNSIGNED DEFAULT 0,
  `timeout` INT UNSIGNED DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `rollbackStatus` varchar(50) DEFAULT NULL,
  `attemptedNoOfRetries` INT UNSIGNED DEFAULT 0,
  `createdAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updatedAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_sm_states` FOREIGN KEY (`stateMachineId`) REFERENCES `StateMachines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table States;
