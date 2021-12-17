--liquibase formatted sql

--changeset raghavender.m:5 runOnChange:false

CREATE INDEX `sTime` ON `ScheduledMessages`(`scheduledTime`);

--rollback DROP INDEX `sTime` ON `ScheduledMessages`;

