-- ============================================================================
-- ERM RBAC ENHANCEMENT SCRIPT - FINAL VERSION
-- ============================================================================
-- Adds resource-based role permissions, branch/department mapping, 
-- and optional role data scope. Extends existing schema without removing 
-- legacy RBAC tables (marked as deprecated).
--
-- MIGRATION NOTES:
-- - Legacy tables (role_right, rights) remain for backward compatibility
-- - All NEW access control uses: resources, role_permissions
-- - Gradual migration: services should use PermissionChecker (resource-based)
-- - Set deprecation flag in role_right: deprecated = 1
-- ============================================================================

-- ============================================================================
-- 1. CORE RESOURCE & PERMISSION SCHEMA
-- ============================================================================

DROP TABLE IF EXISTS `resource_actions`;
DROP TABLE IF EXISTS `role_permissions`;
DROP TABLE IF EXISTS `resources`;

CREATE TABLE `resources` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `resource_code` varchar(120) NOT NULL COMMENT 'Unique identifier: RISK_IDENTIFICATION',
  `name` varchar(150) NOT NULL COMMENT 'Display name for UI',
  `resource_type` varchar(30) NOT NULL COMMENT 'MODULE|PAGE|TAB|FEATURE|CATEGORY',
  `parent_resource_id` bigint DEFAULT NULL COMMENT 'For hierarchical resources',
  `display_order` int DEFAULT NULL COMMENT 'Sort order for UI navigation',
  `active` bit(1) DEFAULT 1 COMMENT 'Enable/disable without deletion',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_resource_active` (`active`,`deleted`),
  KEY `FK_resources_created_by` (`created_by_id`),
  KEY `FK_resources_updated_by` (`updated_by_id`),
  KEY `FK_resources_parent` (`parent_resource_id`),
  CONSTRAINT `FK_resources_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_resources_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_resources_parent` FOREIGN KEY (`parent_resource_id`) REFERENCES `resources` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Resource/Page catalog for permission matrix';

CREATE TABLE `resource_actions` (
  `resource_id` bigint NOT NULL,
  `action` varchar(30) NOT NULL COMMENT 'VIEW|CREATE|EDIT|DELETE|APPROVE|REJECT|EXPORT|PRINT',
  PRIMARY KEY (`resource_id`,`action`),
  KEY `idx_action` (`action`),
  CONSTRAINT `FK_resource_actions_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Allowed actions per resource';

CREATE TABLE `role_permissions` (
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
  `allowed` bit(1) NOT NULL DEFAULT 1 COMMENT 'true=allow, false=deny',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`resource_id`,`action`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_permission_allowed` (`allowed`,`deleted`),
  KEY `FK_role_permissions_created_by` (`created_by_id`),
  KEY `FK_role_permissions_updated_by` (`updated_by_id`),
  CONSTRAINT `FK_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FK_role_permissions_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`),
  CONSTRAINT `FK_role_permissions_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_role_permissions_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Role-to-resource-action mapping';

-- ============================================================================
-- 2. DATA SCOPE MAPPING (BRANCH/DEPARTMENT ISOLATION)
-- ============================================================================

DROP TABLE IF EXISTS `user_department_mapping`;
DROP TABLE IF EXISTS `user_branch_mapping`;

CREATE TABLE `user_branch_mapping` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `user_id` bigint NOT NULL,
  `branch_id` bigint NOT NULL,
  `is_primary` bit(1) DEFAULT 0 COMMENT 'Primary branch for user scope',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_branch` (`user_id`,`branch_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_branch_id` (`branch_id`),
  KEY `idx_is_primary` (`user_id`,`is_primary`),
  CONSTRAINT `FK_user_branch_mapping_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_user_branch_mapping_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK_user_branch_mapping_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_user_branch_mapping_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Multi-branch user access mapping';

CREATE TABLE `user_department_mapping` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `user_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  `is_primary` bit(1) DEFAULT 0 COMMENT 'Primary dept for user scope',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_department` (`user_id`,`department_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_is_primary` (`user_id`,`is_primary`),
  CONSTRAINT `FK_user_department_mapping_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_user_department_mapping_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `FK_user_department_mapping_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_user_department_mapping_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Multi-department user access mapping';

-- ============================================================================
-- 3. OPTIONAL: ROLE-LEVEL DATA SCOPE
-- ============================================================================
-- Use when a role has inherent data access rules beyond user assignment.
-- Example: Manager role can access all departments under assigned branches.

DROP TABLE IF EXISTS `role_data_scope`;

CREATE TABLE `role_data_scope` (
  `id` bigint NOT NULL,
  `client_ip` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `role_id` bigint NOT NULL,
  `branch_id` bigint DEFAULT NULL COMMENT 'NULL = all branches',
  `department_id` bigint DEFAULT NULL COMMENT 'NULL = all departments',
  `allowed` bit(1) DEFAULT 1 COMMENT 'true=allow this scope, false=exclude',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_data_scope` (`role_id`,`branch_id`,`department_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_scope_allowed` (`allowed`,`deleted`),
  CONSTRAINT `FK_role_data_scope_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_role_data_scope_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK_role_data_scope_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `FK_role_data_scope_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_role_data_scope_updated_by` FOREIGN KEY (`updated_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Optional role-level data scope rules';

-- ============================================================================
-- 4. SAMPLE DATA
-- ============================================================================

-- Roles: BASIC_USER and ADVANCED_USER
INSERT INTO `role` (`id`,`code`,`name`,`description`,`priority`,`active`,`deleted`) VALUES
  (9001,'BASIC_USER','Basic User','Basic user role - VIEW only access',10,1,0),
  (9002,'ADVANCED_USER','Advanced User','Advanced user role - FULL access to risk assessment',20,1,0)
ON DUPLICATE KEY UPDATE `code`=VALUES(`code`), `updated_at`=NOW();

-- Resources: Pages and their associated actions
INSERT INTO `resources` (`id`,`resource_code`,`name`,`resource_type`,`display_order`,`active`,`deleted`) VALUES
  (1001,'RISK_IDENTIFICATION','Risk Identification','PAGE',10,1,0),
  (1002,'RISK_ASSESSMENT','Risk Assessment','PAGE',20,1,0),
  (1003,'DASHBOARD','Dashboard','PAGE',1,1,0)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `updated_at`=NOW();

-- Available actions for each resource
INSERT INTO `resource_actions` (`resource_id`,`action`) VALUES
  (1001,'VIEW'), (1001,'CREATE'), (1001,'EDIT'), (1001,'DELETE'),
  (1002,'VIEW'), (1002,'CREATE'), (1002,'EDIT'), (1002,'DELETE'),
  (1003,'VIEW')
ON DUPLICATE KEY UPDATE `action`=VALUES(`action`);

-- Role Permissions: BASIC_USER can only VIEW risk identification
INSERT INTO `role_permissions` (`id`,`role_id`,`resource_id`,`action`,`allowed`,`deleted`) VALUES
  (8001,9001,1001,'VIEW',1,0),
  (8002,9001,1003,'VIEW',1,0)
ON DUPLICATE KEY UPDATE `allowed`=VALUES(`allowed`), `updated_at`=NOW();

-- Role Permissions: ADVANCED_USER can do everything
INSERT INTO `role_permissions` (`id`,`role_id`,`resource_id`,`action`,`allowed`,`deleted`) VALUES
  (8101,9002,1001,'VIEW',1,0),
  (8102,9002,1001,'CREATE',1,0),
  (8103,9002,1001,'EDIT',1,0),
  (8104,9002,1001,'DELETE',1,0),
  (8105,9002,1002,'VIEW',1,0),
  (8106,9002,1002,'CREATE',1,0),
  (8107,9002,1002,'EDIT',1,0),
  (8108,9002,1002,'DELETE',1,0),
  (8109,9002,1003,'VIEW',1,0)
ON DUPLICATE KEY UPDATE `allowed`=VALUES(`allowed`), `updated_at`=NOW();

-- ============================================================================
-- 5. MIGRATION MARKER
-- ============================================================================
-- Mark legacy role_right table as deprecated for transition tracking
ALTER TABLE `role_right` ADD COLUMN IF NOT EXISTS `deprecated` bit(1) DEFAULT 0 COMMENT 'Deprecated: Use role_permissions instead';

-- ============================================================================
-- END RBAC ENHANCEMENT
-- ============================================================================
