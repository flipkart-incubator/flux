CREATE TABLE IF NOT EXISTS `audit_records` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `state_machine_name` VARCHAR(255) NOT NULL,
  `state_machine_instance_id` VARCHAR(255) NOT NULL,
  `state_id` bigint(20) NOT NULL,
  `retry_attempt` TINYINT UNSIGNED DEFAULT NULL,
  `state_status` VARCHAR(100) DEFAULT NULL,
  `state_start_time` DATETIME(3) DEFAULT NULL,
  `state_end_time` DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_audit_on_SM_instance_id` (`state_machine_instance_id`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;


CREATE TABLE IF NOT EXISTS `checkpoints` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `state_machine_name` VARCHAR(255) NOT NULL,
  `state_machine_instance_id` VARCHAR(255) NOT NULL,
  `state_id` BIGINT(20) NOT NULL,
  `data` LONGTEXT,
  `created_at` DATETIME(3) DEFAULT NULL,
  `updated_at` DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_checkpoints_on_SM_instance_id_and_state_id` (`state_machine_instance_id`,`state_id`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;


CREATE TABLE IF NOT EXISTS `events` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `type` VARCHAR(100) NOT NULL,
  `status` VARCHAR(100) DEFAULT NULL,
  `state_machine_instance_id` VARCHAR(255) NOT NULL,
  `event_data` LONGTEXT,
  `event_source` VARCHAR(100) DEFAULT NULL,
  `created_at` DATETIME(3) DEFAULT NULL,
  `updated_at` DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;


CREATE TABLE IF NOT EXISTS `state_machines` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `version` TINYINT UNSIGNED NOT NULL,
  `start_state_id` BIGINT(20) NOT NULL,
  `description` VARCHAR(300) DEFAULT NULL,
  `created_at` DATETIME(3) DEFAULT NULL,
  `updated_at` DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;


CREATE TABLE IF NOT EXISTS `states` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `version` TINYINT UNSIGNED NOT NULL,
  `description` VARCHAR(300) DEFAULT NULL,
  `state_machine_id` BIGINT(20) NOT NULL,
  `dependencies` VARCHAR(1000) DEFAULT NULL,
  `hook_class_name` VARCHAR(255) DEFAULT NULL,
  `task_class_name` VARCHAR(255) NOT NULL,
  `retry_count` TINYINT UNSIGNED DEFAULT NULL,
  `timeout` SMALLINT UNSIGNED DEFAULT NULL,
  `created_at` DATETIME(3) DEFAULT NULL,
  `updated_at` DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_sm_states` FOREIGN KEY (`state_machine_id`) REFERENCES `state_machines` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE=InnoDB
ROW_FORMAT=DEFAULT
DEFAULT CHARSET=utf8
AUTO_INCREMENT=1;


commit;