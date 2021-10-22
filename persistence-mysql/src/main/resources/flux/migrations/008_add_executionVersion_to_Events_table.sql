--liquibase formatted sql

--changeset akif.khan:8 runOnChange:false

ALTER TABLE `Events`
  DROP PRIMARY KEY,
  ADD COLUMN `executionVersion` INT UNSIGNED NOT NULL DEFAULT 0,
  ADD PRIMARY KEY(`stateMachineInstanceId`, `name`,`executionVersion`);