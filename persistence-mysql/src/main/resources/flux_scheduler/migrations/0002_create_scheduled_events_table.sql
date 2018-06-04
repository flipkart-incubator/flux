--liquibase formatted sql

--changeset shyam.akirala:2 runOnChange:false

CREATE TABLE `ScheduledEvents` (
  `correlationId` VARCHAR(64) NOT NULL,
  `eventName` VARCHAR(255) NOT NULL,
  `scheduledTime` bigint(20) NOT NULL,
  `eventData` MEDIUMBLOB,
  PRIMARY KEY (`correlationId`,`eventName`),
  KEY `sTime` (`scheduledTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;