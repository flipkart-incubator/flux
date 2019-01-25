--liquibase formatted sql

--changeset shyam.akirala:3 runOnChange:false

CREATE TABLE IF NOT EXISTS `AuditRecords` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stateMachineInstanceId` VARCHAR(64) NOT NULL,
  `stateId` BIGINT NOT NULL,
  `executionVersion` smallint(5) unsigned NOT NULL,
  `retryAttempt` INT UNSIGNED DEFAULT NULL,
  `stateStatus` VARCHAR(50) DEFAULT NULL,
  `stateRollbackStatus` VARCHAR(50) DEFAULT NULL,
  `errors` VARCHAR(1000) DEFAULT NULL,
  `createdAt` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `index_audit_on_SM_instance_id` (`stateMachineInstanceId`),
  CONSTRAINT `FK_audit_stateMachineId` FOREIGN KEY (`stateMachineInstanceId`) REFERENCES `StateMachines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table AuditRecords;
