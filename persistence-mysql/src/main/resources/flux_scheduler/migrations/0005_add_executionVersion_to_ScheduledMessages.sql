--liquibase formatted sql

--changeset akif.khan:4 runOnChange:false

ALTER TABLE `ScheduledMessages`
  DROP PRIMARY KEY,
  ADD COLUMN `executionVersion` INT UNSIGNED NOT NULL DEFAULT 0,
  ADD PRIMARY KEY (`taskId`,`stateMachineId`,`executionVersion`);