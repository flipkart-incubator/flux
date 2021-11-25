--liquibase formatted sql

--changeset akif.khan:5 runOnChange:false

ALTER TABLE `ScheduledMessages`
  DROP PRIMARY KEY,
  ADD COLUMN `executionVersion` INT UNSIGNED NOT NULL DEFAULT 0,
  ADD PRIMARY KEY (`taskId`,`stateMachineId`,`executionVersion`);