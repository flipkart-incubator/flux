--liquibase formatted sql

--changeset akif.khan:1 runOnChange:false

CREATE TABLE IF NOT EXISTS `StateTraversalPath` (
  `stateId` BIGINT NOT NULL ,
  `stateMachineId` VARCHAR (64) NOT NULL ,
  `nextDependentStates` VARCHAR(1000) DEFAULT NULL,
  `createdAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`stateMachineId`, `stateId`),
  CONSTRAINT `SM_stateTraversalPath` FOREIGN KEY (`stateMachineId`) REFERENCES `StateMachines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table StateTraversalPath;