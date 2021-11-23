--liquibase formatted sql

--changeset vartika.bhatia:12 runOnChange:false

ALTER TABLE `States`
    ADD COLUMN `maxreplayableRetries` SMALLINT UNSIGNED NOT NULL DEFAULT 5,
    ADD COLUMN `attemptedNumOfReplayableRetries` SMALLINT UNSIGNED NOT NULL DEFAULT 0;

--rollback Alter Table `States` Drop Column `maxreplayableRetries`, Drop Column `attemptedNumOfReplayableRetries`;