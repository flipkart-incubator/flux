--liquibase formatted sql

--changeset akif.khan:1 runOnChange:false

CREATE TABLE `StateTransition` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `executionVersion` smallint(5) unsigned NOT NULL,
  `dependencies` blob,
  `stateMachineId` varchar(64) NOT NULL,
  `status` enum('initialized','running','completed','cancelled','errored','sidelined','unsidelined') DEFAULT NULL,
  `validity` enum('yes','no') DEFAULT NULL,
  `outputEvent` varchar(1000) DEFAULT NULL,
  `rollbackStatus` varchar(50) DEFAULT NULL,
  `attemptedNoOfRetries` int(10) unsigned DEFAULT '0',
  `createdAt` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updatedAt` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`stateMachineId`,`id`,`executionVersion`),
  KEY `index_stateMachineId` (`stateMachineId`),
  CONSTRAINT `FK_sm_stateTransition` FOREIGN KEY (`stateMachineId`) REFERENCES `StateMachines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--rollback drop table StateTrasition;