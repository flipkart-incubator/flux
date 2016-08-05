--liquibase formatted sql

--changeset yogesh.nachnani:3 runOnChange:false
alter table job add column trigger_time bigint(20) DEFAULT NULL ;
alter table job modify column schedule_id bigint(20);
alter table job modify column side_lined bit(1) default 0;