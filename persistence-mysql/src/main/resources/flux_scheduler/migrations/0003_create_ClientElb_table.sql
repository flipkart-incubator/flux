--liquibase formatted sql

--changeset akif.khan:3 runOnChange:false

CREATE TABLE IF NOT EXISTS `ClientElb` (
  `id` VARCHAR(64) NOT NULL,
  `elbUrl` VARCHAR(300) NOT NULL,
  `createdAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updatedAt` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;

--rollback drop table ClientElb;
