--liquibase formatted sql

--changeset shyam.akirala:4 runOnChange:false

INSERT INTO ClientElb(id, elbUrl) VALUES('defaultElbId', 'http://localhost:9997');