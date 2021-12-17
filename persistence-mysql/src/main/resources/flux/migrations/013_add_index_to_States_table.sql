--liquibase formatted sql

--changeset raghavender.m:13 runOnChange:false

CREATE INDEX `cTime` ON `States`(`createdAt`);

--rollback DROP INDEX `cTime` ON `States`;
