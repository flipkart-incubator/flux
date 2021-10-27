--liquibase formatted sql

--changeset akif.khan:7 runOnChange:false

ALTER TABLE `States` ADD COLUMN `executionVersion` INT UNSIGNED DEFAULT 0,
    ADD COLUMN `replayable` enum('true','false') DEFAULT 'false';