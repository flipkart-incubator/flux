--liquibase formatted sql

--changeset akif.khan:1 runOnChange:false

CREATE TABLE `StatesMetaData` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `version` smallint(5) unsigned NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `dependencies` varchar(1000) DEFAULT NULL,
  `stateMachineId` varchar(64) NOT NULL,
  `task` varchar(1000) DEFAULT NULL,
  `outputEvent` varchar(500) DEFAULT NULL,
  `retryCount` int(10) unsigned DEFAULT '0',
  `timeout` int(10) unsigned DEFAULT NULL,
  `createdAt` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`stateMachineId`,`id`),
  KEY `index_stateMachineId` (`stateMachineId`),
  CONSTRAINT `FK_sm_statesMetaData` FOREIGN KEY (`stateMachineId`) REFERENCES `StateMachines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--rollback drop table StateMetaData;