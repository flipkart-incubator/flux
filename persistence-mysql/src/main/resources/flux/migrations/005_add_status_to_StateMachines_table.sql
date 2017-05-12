--liquibase formatted sql

--changeset shyam.akirala:5 runOnChange:false

ALTER TABLE `StateMachines` ADD COLUMN `status` varchar(50) DEFAULT NULL;
