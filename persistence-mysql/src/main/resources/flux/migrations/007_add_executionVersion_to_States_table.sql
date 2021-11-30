--liquibase formatted sql

--changeset akif.khan:7 runOnChange:false

ALTER TABLE `States`
  ADD COLUMN `executionVersion` INT UNSIGNED NOT NULL DEFAULT 0,
  ADD COLUMN `replayable` BIT NOT NULL DEFAULT 0;

--rollback Alter Table `States` Drop Column `executionVersion`, Drop Column `replayable`;