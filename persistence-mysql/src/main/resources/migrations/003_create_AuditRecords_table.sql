--liquibase formatted sql

--changeset shyam.akirala:3 runOnChange:false

CREATE TABLE IF NOT EXISTS `AuditRecords` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stateMachineInstanceId` VARCHAR(100) NOT NULL,
  `stateId` VARCHAR(100) NOT NULL,
  `retryAttempt` TINYINT UNSIGNED DEFAULT NULL,
  `stateStatus` VARCHAR(100) DEFAULT NULL,
  `stateRollbackStatus` VARCHAR(100) DEFAULT NULL,
  `errors` VARCHAR(1000) DEFAULT NULL,
  `createdAt` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `index_audit_on_SM_instance_id` (`stateMachineInstanceId`),
  CONSTRAINT `FK_sm_audit` FOREIGN KEY (`stateMachineInstanceId`) REFERENCES `StateMachines` (`id`),
  CONSTRAINT `FK_state_audit` FOREIGN KEY (`stateId`) REFERENCES `States` (`id`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table AuditRecords;
