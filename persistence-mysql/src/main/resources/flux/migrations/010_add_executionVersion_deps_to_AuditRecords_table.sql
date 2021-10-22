--liquibase formatted sql

--changeset akif.khan:10 runOnChange:false

ALTER TABLE `AuditRecords`
  ADD COLUMN `taskExecutionVersion` INT UNSIGNED NOT NULL DEFAULT 0,
  ADD COLUMN `eventDependencies` VARCHAR(1000) DEFAULT NULL;

--rollback Alter Table `AuditRecords` Drop Column `taskExecutionVersion`, Drop Column `eventDependencies`;