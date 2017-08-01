--liquibase formatted sql

--changeset shyam.akirala:5 runOnChange:false

ALTER TABLE `StateMachines` DROP COLUMN `correlationId` varchar(50) DEFAULT NULL;

--rollback Alter Table `StateMachines` Drop Column `status`;