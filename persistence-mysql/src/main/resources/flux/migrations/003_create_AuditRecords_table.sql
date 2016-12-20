--liquibase formatted sql

--changeset shyam.akirala:3 runOnChange:false

CREATE TABLE IF NOT EXISTS `AuditRecords` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stateMachineInstanceId` BIGINT NOT NULL,
  `stateId` BIGINT NOT NULL,
  `retryAttempt` INT UNSIGNED DEFAULT NULL,
  `stateStatus` VARCHAR(50) DEFAULT NULL,
  `stateRollbackStatus` VARCHAR(50) DEFAULT NULL,
  `errors` VARCHAR(1000) DEFAULT NULL,
  `createdAt` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `index_audit_on_SM_instance_id` (`stateMachineInstanceId`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table AuditRecords;
