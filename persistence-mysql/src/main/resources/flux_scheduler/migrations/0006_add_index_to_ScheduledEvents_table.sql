--liquibase formatted sql

--changeset raghavender.m:6 runOnChange:false

CREATE INDEX `sTime` ON `ScheduledEvents`(`scheduledTime`);

--rollback DROP INDEX `sTime` ON `ScheduledEvents`;

