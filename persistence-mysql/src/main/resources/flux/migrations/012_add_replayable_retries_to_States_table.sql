--liquibase formatted sql

--changeset vartika.bhatia:12 runOnChange:false

ALTER TABLE `States`
    ADD COLUMN `maxreplayableRetries` SMALLINT UNSIGNED DEFAULT 5,
    ADD COLUMN `attemptedNumOfReplayableRetries` SMALLINT UNSIGNED DEFAULT 0;