--liquibase formatted sql

--changeset akif.khan:6 runOnChange:false

ALTER TABLE `StateMachines` ADD COLUMN `clientElbId` varchar(64) NOT NULL DEFAULT 'defaultElbId';

--rollback Alter Table `StateMachines` Drop Column `clientElbId`;