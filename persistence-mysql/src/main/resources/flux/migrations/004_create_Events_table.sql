--liquibase formatted sql

--changeset shyam.akirala:4 runOnChange:false

CREATE TABLE IF NOT EXISTS `Events` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `type` VARCHAR(100) NOT NULL,
  `status` VARCHAR(50) DEFAULT NULL,
  `stateMachineInstanceId` BIGINT NOT NULL,
  `eventData` BLOB,
  `eventSource` VARCHAR(100) DEFAULT NULL,
  `createdAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updatedAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY (`stateMachineInstanceId`, `name`),
  CONSTRAINT `FK_sm_events` FOREIGN KEY (`stateMachineInstanceId`) REFERENCES `StateMachines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table Events;
