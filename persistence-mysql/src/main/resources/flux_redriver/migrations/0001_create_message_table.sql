--liquibase formatted sql

--changeset yogesh.nachnani:1 runOnChange:false

CREATE TABLE `ScheduledMessages` (
  `taskId` bigint(20) NOT NULL AUTO_INCREMENT,
  `scheduledTime` bigint(20) NOT NULL,
  PRIMARY KEY (`taskId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;