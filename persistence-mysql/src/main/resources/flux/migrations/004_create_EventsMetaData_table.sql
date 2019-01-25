--liquibase formatted sql

--changeset akif.khan:1 runOnChange:false

CREATE TABLE `EventsMetaData` (
  `name` varchar(255) NOT NULL,
  `type` varchar(100) NOT NULL,
  `stateMachineId` varchar(64) NOT NULL,
  `dependentStates` blob,
  `createdAt` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`stateMachineId`,`name`),
  CONSTRAINT `FK_sm_eventsMetaData` FOREIGN KEY (`stateMachineId`) REFERENCES `StateMachines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--rollback drop table EventsMetaData;