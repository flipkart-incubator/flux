--liquibase formatted sql

--changeset akif.khan:9 runOnChange:false

ALTER TABLE `StateMachines`
  ADD COLUMN `executionVersion` INT UNSIGNED NOT NULL DEFAULT 0;

--rollback Alter Table `StateMachines` Drop Column `executionVersion`;