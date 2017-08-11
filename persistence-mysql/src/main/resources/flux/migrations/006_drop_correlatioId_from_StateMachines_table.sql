--liquibase formatted sql

--changeset amitkumar.o:6 runOnChange:false

ALTER TABLE `StateMachines` DROP COLUMN `correlationId` ;

--rollback Alter Table `StateMachines` Add Column `correlationId` VARCHAR(128) DEFAULT NULL;