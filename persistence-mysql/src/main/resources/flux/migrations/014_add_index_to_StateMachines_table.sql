--liquibase formatted sql

--changeset raghavender.m:14 runOnChange:false

CREATE INDEX `cTime` ON `StateMachines`(`createdAt`);

--rollback DROP INDEX `cTime` ON `StateMachines`;

