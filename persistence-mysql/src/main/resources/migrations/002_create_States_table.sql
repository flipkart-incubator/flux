--liquibase formatted sql

--changeset shyam.akirala:2 runOnChange:false

CREATE TABLE IF NOT EXISTS `States` (
  `id` VARCHAR(100) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `version` SMALLINT UNSIGNED NOT NULL,
  `description` VARCHAR(300) DEFAULT NULL,
  `stateMachineId` VARCHAR(100) DEFAULT NULL,
  `dependencies` VARCHAR(1000) DEFAULT NULL,
  `onEntryHook` varchar(500) DEFAULT NULL,
  `task` VARCHAR(500) DEFAULT NULL,
  `onExitHook` varchar(500) DEFAULT NULL,
  `retryCount` TINYINT UNSIGNED DEFAULT NULL,
  `timeout` SMALLINT UNSIGNED DEFAULT NULL,
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