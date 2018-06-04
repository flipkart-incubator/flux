--liquibase formatted sql

--changeset yogesh.nachnani:1 runOnChange:false

CREATE TABLE `ScheduledMessages` (
  `taskId` bigint(20) NOT NULL,
  `stateMachineId` VARCHAR (64) NOT NULL,
  `scheduledTime` bigint(20) NOT NULL,
  PRIMARY KEY (`stateMachineId` , `taskId` ),
  KEY `sTime` (`scheduledTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;