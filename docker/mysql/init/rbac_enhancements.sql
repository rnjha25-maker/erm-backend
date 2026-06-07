-- RBAC enhancement script for ERM
-- Adds resource-based role permissions, branch/department mapping, and optional role data scope.
-- This script extends the existing MySQL schema without removing legacy RBAC tables.

CREATE TABLE IF NOT EXISTS `resources` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `resource_code` varchar(120) NOT NULL,
  `name` varchar(150) NOT NULL,
  `resource_type` varchar(30) NOT NULL,
  `parent_resource_id` bigint DEFAULT NULL,
  `display_order` int DEFAULT NULL,
  `active` bit(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`),
  KEY `FK_resources_created_by` (`created_by_id`),
  KEY `FK_resources_updated_by` (`updated_by_id`),
  KEY `FK_resources_parent` (`parent_resource_id`),
  CONSTRAINT `FK_resources_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_resources_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_resources_parent` FOREIGN KEY (`parent_resource_id`) REFERENCES `resources` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `resource_actions` (
  `resource_id` bigint NOT NULL,
  `action` varchar(30) NOT NULL,
  PRIMARY KEY (`resource_id`,`action`),
  KEY `FK_resource_actions_resource` (`resource_id`),
  CONSTRAINT `FK_resource_actions_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `role_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `action` varchar(30) NOT NULL,
  `allowed` bit(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`resource_id`,`action`),
  KEY `FK_role_permissions_role` (`role_id`),
  KEY `FK_role_permissions_resource` (`resource_id`),
  KEY `FK_role_permissions_created_by` (`created_by_id`),
  KEY `FK_role_permissions_updated_by` (`updated_by_id`),
  CONSTRAINT `FK_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FK_role_permissions_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`),
  CONSTRAINT `FK_role_permissions_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_role_permissions_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_branch_mapping` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `user_id` bigint NOT NULL,
  `branch_id` bigint NOT NULL,
  `is_primary` bit(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_branch` (`user_id`,`branch_id`),
  KEY `FK_user_branch_mapping_user` (`user_id`),
  KEY `FK_user_branch_mapping_branch` (`branch_id`),
  CONSTRAINT `FK_user_branch_mapping_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_user_branch_mapping_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK_user_branch_mapping_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_user_branch_mapping_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_department_mapping` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `user_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  `is_primary` bit(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_department` (`user_id`,`department_id`),
  KEY `FK_user_department_mapping_user` (`user_id`),
  KEY `FK_user_department_mapping_department` (`department_id`),
  CONSTRAINT `FK_user_department_mapping_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_user_department_mapping_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `FK_user_department_mapping_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_user_department_mapping_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `role_data_scope` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `role_id` bigint NOT NULL,
  `branch_id` bigint DEFAULT NULL,
  `department_id` bigint DEFAULT NULL,
  `allowed` bit(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_data_scope` (`role_id`,`branch_id`,`department_id`),
  KEY `FK_role_data_scope_role` (`role_id`),
  KEY `FK_role_data_scope_branch` (`branch_id`),
  KEY `FK_role_data_scope_department` (`department_id`),
  CONSTRAINT `FK_role_data_scope_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FK_role_data_scope_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK_role_data_scope_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `FK_role_data_scope_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_role_data_scope_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Sample modules/resources and permissions for Basic and Advanced roles.
-- NOTE: Adjust IDs to avoid collisions with existing data in the current schema.

INSERT INTO `resources` (`id`,`resource_code`,`name`,`resource_type`,`display_order`,`active`,`deleted`) VALUES
  (1001,'RISK_IDENTIFICATION','Risk Identification', 'PAGE', 10, 1, 0),
  (1002,'RISK_ASSESSMENT','Risk Assessment', 'PAGE', 20, 1, 0);

INSERT INTO `resource_actions` (`resource_id`,`action`) VALUES
  (1001,'VIEW'),
  (1001,'CREATE'),
  (1001,'EDIT'),
  (1001,'DELETE'),
  (1002,'VIEW'),
  (1002,'CREATE'),
  (1002,'EDIT'),
  (1002,'DELETE');

INSERT INTO `role` (`id`,`name`,`description`,`priority`,`deleted`) VALUES
  (9001,'BASIC_USER','Basic user role - page view permission only',10,0),
  (9002,'ADVANCED_USER','Advanced user role - full access to risk assessment',20,0);

INSERT INTO `role_permissions` (`id`,`role_id`,`resource_id`,`action`,`allowed`,`deleted`) VALUES
  (8101,9001,1001,'VIEW',1,0),
  (8102,9002,1002,'VIEW',1,0),
  (8103,9002,1002,'CREATE',1,0),
  (8104,9002,1002,'EDIT',1,0),
  (8105,9002,1002,'DELETE',1,0);

-- Example branch/department mapping.
-- If your current user table already stores a primary branch/department, use these tables for additional allowed scopes.

INSERT INTO `user_branch_mapping` (`id`,`user_id`,`branch_id`,`is_primary`,`deleted`) VALUES
  (7001,2001,3218,1,0),
  (7002,2002,5302,1,0);

INSERT INTO `user_department_mapping` (`id`,`user_id`,`department_id`,`is_primary`,`deleted`) VALUES
  (7101,2001,3224,1,0),
  (7102,2002,3243,1,0);

-- Optional role data scope: grant roles allowed branch/department access beyond a user’s own assignment.
INSERT INTO `role_data_scope` (`id`,`role_id`,`branch_id`,`department_id`,`allowed`,`deleted`) VALUES
  (7201,9002,5302,NULL,1,0),
  (7202,9002,NULL,3224,1,0);
