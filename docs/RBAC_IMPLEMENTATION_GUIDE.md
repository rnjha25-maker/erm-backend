# RBAC Implementation - Migration & Implementation Guide

## Overview
This document guides the transition from **legacy role-based access control** (role_right/rights/modules) to the **new resource-based RBAC model** (resources/role_permissions) in the ERM system.

## Architecture

### Two Models in Coexistence
- **Legacy Model** (Deprecated): `role_right`, `rights`, `modules` tables
- **New Model** (Recommended): `resources`, `resource_actions`, `role_permissions` tables
- **Data Scope**: `user_branch_mapping`, `user_department_mapping` for multi-tenant isolation
- **Optional**: `role_data_scope` for role-level scope enforcement

## Database Schema

### Core Tables (New Model)

#### 1. `resources`
Defines pages, modules, features accessible in the system.
```sql
CREATE TABLE resources (
  id BIGINT PRIMARY KEY,
  resource_code VARCHAR(120) UNIQUE,      -- "RISK_IDENTIFICATION"
  name VARCHAR(150),                      -- "Risk Identification"
  resource_type VARCHAR(30),              -- PAGE | MODULE | FEATURE
  parent_resource_id BIGINT,              -- Hierarchical resources
  display_order INT,                      -- For UI ordering
  active BIT DEFAULT 1,
  deleted BIT DEFAULT 0,
  created_at DATETIME(6),
  created_by_id BIGINT
);
```

#### 2. `resource_actions`
Defines allowed actions per resource.
```sql
CREATE TABLE resource_actions (
  resource_id BIGINT,
  action VARCHAR(30),  -- VIEW | CREATE | EDIT | DELETE | APPROVE | REJECT | EXPORT | PRINT
  PRIMARY KEY (resource_id, action)
);
```

#### 3. `role_permissions`
Links roles to resource-action combinations with allow/deny flag.
```sql
CREATE TABLE role_permissions (
  id BIGINT PRIMARY KEY,
  role_id BIGINT,                -- User's role
  resource_id BIGINT,            -- Resource (page/module)
  action VARCHAR(30),            -- Action to perform
  allowed BIT DEFAULT 1,         -- 1=allow, 0=deny
  deleted BIT DEFAULT 0,
  created_at DATETIME(6)
);
```

### Data Scope Tables

#### 4. `user_branch_mapping`
Assign multiple branches to a user for multi-branch access.
```sql
CREATE TABLE user_branch_mapping (
  id BIGINT PRIMARY KEY,
  user_id BIGINT,
  branch_id BIGINT,
  is_primary BIT DEFAULT 0,  -- Primary branch for defaults
  deleted BIT DEFAULT 0
);
```

#### 5. `user_department_mapping`
Assign multiple departments to a user for multi-department access.
```sql
CREATE TABLE user_department_mapping (
  id BIGINT PRIMARY KEY,
  user_id BIGINT,
  department_id BIGINT,
  is_primary BIT DEFAULT 0,  -- Primary dept for defaults
  deleted BIT DEFAULT 0
);
```

#### 6. `role_data_scope` (Optional)
Define role-level scope rules (e.g., "Manager role can see all departments in its branch").
```sql
CREATE TABLE role_data_scope (
  id BIGINT PRIMARY KEY,
  role_id BIGINT,
  branch_id BIGINT,           -- NULL = all branches
  department_id BIGINT,       -- NULL = all departments
  allowed BIT DEFAULT 1
);
```

## Java Implementation

### Key Components

#### 1. DataScopeUtil
Fetches and manages user's branch/department access.
```java
DataScopeUtil dataScopeUtil; // Injected

// Get user's allowed branches
List<Long> branchIds = dataScopeUtil.getAllowedBranchIds(user);

// Get user's allowed departments
List<Long> deptIds = dataScopeUtil.getAllowedDepartmentIds(user);

// Get complete scope object
DataScope scope = dataScopeUtil.getUserDataScope(user);
```

#### 2. DataScopeSpecification
Reusable JPA Specification for Criteria API filtering.
```java
// Service layer usage
List<RiskIdentification> risks = riskRepository.findAll(
    DataScopeSpecification.byBranches(userScope.allowedBranchIds)
    .and(DataScopeSpecification.byDepartments(userScope.allowedDepartmentIds))
);
```

#### 3. Enhanced RolePermissionRepository
Core queries with status checking (active, not deleted).
```java
// Check if user can perform action
boolean canCreate = rolePermissionRepository.existsAllowedPermission(
    userId, 
    "RISK_IDENTIFICATION", 
    PermissionAction.CREATE
);

// Get all user permissions
List<RolePermission> permissions = rolePermissionRepository.findAllowedByUserId(userId);
```

#### 4. Data Scope Repositories
Manage user branch/department mappings.
```java
UserBranchMappingRepository branchMappingRepository;
List<Long> branchIds = branchMappingRepository.findAllowedBranchIdsByUserId(userId);

UserDepartmentMappingRepository deptMappingRepository;
List<Long> deptIds = deptMappingRepository.findAllowedDepartmentIdsByUserId(userId);
```

### Service Layer Pattern

```java
@Service
public class RiskIdentificationService {

    @Autowired
    private RiskIdentificationRepository riskRepository;
    
    @Autowired
    private PermissionChecker permissionChecker;
    
    @Autowired
    private DataScopeUtil dataScopeUtil;

    /**
     * Get all risk identifications visible to user.
     * Enforces: (1) Action permission (2) Data scope (branch/department)
     */
    public List<RiskIdentification> getUserRisks(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // Step 1: Check action permission
        permissionChecker.requirePermission(userId, "RISK_IDENTIFICATION", PermissionAction.VIEW);
        
        // Step 2: Get user's data scope
        DataScope scope = dataScopeUtil.getUserDataScope(user);
        
        // Step 3: Query with scope filtering
        return riskRepository.findAll(
            DataScopeSpecification.fromDataScope(scope)
            .and(notDeleted())
        );
    }

    /**
     * Create risk - requires CREATE permission + data scope
     */
    public RiskIdentification createRisk(Long userId, RiskIdentificationDTO dto) {
        permissionChecker.requirePermission(userId, "RISK_IDENTIFICATION", PermissionAction.CREATE);
        
        User user = userRepository.findById(userId).orElseThrow();
        
        // Validate DTO branch/dept exist in user's scope
        if (!dataScopeUtil.canAccessBranch(user, dto.getBranchId())) {
            throw new AccessDeniedException("User cannot access this branch");
        }
        if (!dataScopeUtil.canAccessDepartment(user, dto.getDepartmentId())) {
            throw new AccessDeniedException("User cannot access this department");
        }
        
        // Create with audit fields
        RiskIdentification risk = new RiskIdentification();
        risk.setName(dto.getName());
        risk.setBranch(branchRepository.findById(dto.getBranchId()).orElseThrow());
        risk.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElseThrow());
        
        return riskRepository.save(risk);
    }
}
```

### API Response Format

```java
@RestController
@RequestMapping("/api/rbac")
public class RbacController {

    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    /**
     * Get user's accessible resources with permission matrix.
     * Response format: UI-friendly, suitable for dynamic menu/permission display.
     */
    @GetMapping("/user-resources")
    public List<ResourceWithPermissionsResponse> getUserResources(@RequestHeader("X-user-Id") Long userId) {
        List<Resource> resources = resourceRepository.findByActiveTrue();
        
        return resources.stream()
            .map(resource -> {
                ResourceWithPermissionsResponse response = new ResourceWithPermissionsResponse();
                response.setResourceId(resource.getId());
                response.setResourceCode(resource.getResourceCode());
                response.setResourceName(resource.getName());
                response.setResourceType(resource.getResourceType().toString());
                response.setDisplayOrder(resource.getDisplayOrder());
                
                // Populate permissions
                Map<String, Boolean> permissions = new HashMap<>();
                for (PermissionAction action : resource.getSupportedActions()) {
                    boolean allowed = rolePermissionRepository.existsAllowedPermission(
                        userId, 
                        resource.getResourceCode(), 
                        action
                    );
                    permissions.put(action.toString(), allowed);
                }
                response.setPermissions(permissions);
                
                return response;
            })
            .filter(ResourceWithPermissionsResponse::hasAnyPermission)
            .collect(Collectors.toList());
    }
}
```

## Migration Steps

### Phase 1: Database Setup
1. Run `docker/mysql/init/rbac_enhancements_v2.sql` in MySQL
2. Verify all 6 new tables created without errors
3. Confirm sample data loaded (roles, resources, permissions)

### Phase 2: Java Entities
1. Ensure `Resource`, `ResourceType`, `PermissionAction` entities exist
2. Create `UserBranchMapping` and `UserDepartmentMapping` entities with JPA annotations
3. Update `User` entity with new relationships:
   ```java
   @ManyToMany
   @JoinTable(name = "user_branch_mapping")
   private List<Branch> allowedBranches;
   
   @ManyToMany
   @JoinTable(name = "user_department_mapping")
   private List<Department> allowedDepartments;
   ```

### Phase 3: Repositories
1. Implement `UserBranchMappingRepository` with custom JPQL queries
2. Implement `UserDepartmentMappingRepository` with custom JPQL queries
3. Enhance `RolePermissionRepository` with data scope methods (already done)
4. Update `ResourceRepository` to support `JpaSpecificationExecutor`

### Phase 4: Utilities
1. Create `DataScopeUtil` class (permission boundary queries)
2. Create `DataScopeSpecification` class (JPA Specification builders)

### Phase 5: Service Layer
1. Update all `@Service` classes to call permission checks + data scope filters
2. Add `DataScopeUtil` injection
3. Wrap repository queries with `DataScopeSpecification`

### Phase 6: Controllers & DTOs
1. Create `ResourceWithPermissionsResponse` DTO
2. Add `/api/rbac/user-resources` endpoint
3. Update existing endpoints to return permission matrix in responses

### Phase 7: Deprecation Marking
1. Add `@Deprecated` annotation to legacy entities (RoleRight, Right, Modules)
2. Mark legacy tables with `deprecated` column
3. Document timeline for complete removal

## Sample Data & Test Scenarios

### Roles Setup
```sql
INSERT INTO role (id, code, name, priority, active, deleted)
VALUES 
  (9001, 'BASIC_USER', 'Basic User', 10, 1, 0),
  (9002, 'ADVANCED_USER', 'Advanced User', 20, 1, 0);
```

### Resources Setup
```sql
INSERT INTO resources (id, resource_code, name, resource_type, display_order, active)
VALUES
  (1001, 'RISK_IDENTIFICATION', 'Risk Identification', 'PAGE', 10, 1),
  (1002, 'RISK_ASSESSMENT', 'Risk Assessment', 'PAGE', 20, 1);
```

### Permission Assignments
```sql
-- BASIC_USER: Can only VIEW risk identification
INSERT INTO role_permissions (id, role_id, resource_id, action, allowed)
VALUES (8001, 9001, 1001, 'VIEW', 1);

-- ADVANCED_USER: Can do everything on both resources
INSERT INTO role_permissions (id, role_id, resource_id, action, allowed)
VALUES 
  (8101, 9002, 1001, 'VIEW', 1),
  (8102, 9002, 1001, 'CREATE', 1),
  (8103, 9002, 1001, 'EDIT', 1),
  (8104, 9002, 1001, 'DELETE', 1),
  (8105, 9002, 1002, 'VIEW', 1),
  (8106, 9002, 1002, 'CREATE', 1),
  (8107, 9002, 1002, 'EDIT', 1),
  (8108, 9002, 1002, 'DELETE', 1);
```

### Test Scenarios

#### Scenario 1: BASIC_USER views RISK_IDENTIFICATION
```
Input: userId=2001 (BASIC_USER), resource=RISK_IDENTIFICATION, action=VIEW
Expected: ✓ ALLOWED (has VIEW permission)
Actual Query: SELECT EXISTS(permission entry) WHERE role=BASIC_USER AND resource=RISK_ID AND action=VIEW AND allowed=1
```

#### Scenario 2: BASIC_USER creates RISK_IDENTIFICATION
```
Input: userId=2001 (BASIC_USER), resource=RISK_IDENTIFICATION, action=CREATE
Expected: ✗ DENIED (no CREATE permission)
Actual Query: SELECT EXISTS(...) = 0 rows → Permission denied
```

#### Scenario 3: ADVANCED_USER with branch scope
```
Input: userId=2002 (ADVANCED_USER), branch_id=3218, department_id=3224
Query: Find all risks where branch IN (3218) AND department IN (3224)
Expected: Only risks owned by that branch/dept are visible
Implementation: DataScopeSpecification.fromDataScope(scope)
```

## Backward Compatibility

### Legacy Support
- Old `role_right` entries still functional for existing code
- New code uses `resources` and `role_permissions`
- Both systems can coexist during transition

### Migration Path (Optional)
```sql
-- One-time migration: Convert role_right to role_permissions
-- Map old modules to new resources by resource_code matching
-- Run after all code is deployed

INSERT INTO role_permissions (role_id, resource_id, action, allowed)
SELECT rr.role_id, r.id, 'VIEW', 1
FROM role_right rr
JOIN right rt ON rr.right_id = rt.id
JOIN modules m ON rt.module_id = m.id
JOIN resources r ON UPPER(m.name) = UPPER(r.resource_code)
WHERE rr.deleted = 0 AND rt.deleted = 0 AND m.deleted = 0;
```

## Troubleshooting

### Issue: Permission denied when shouldn't be
1. Check role is assigned to user: `SELECT * FROM user_role WHERE user_id = ? AND deleted = 0;`
2. Check role has permission: `SELECT * FROM role_permissions WHERE role_id = ? AND resource_code = ? AND deleted = 0;`
3. Check resource is active: `SELECT * FROM resources WHERE id = ? AND active = 1 AND deleted = 0;`
4. Check action name matches: VIEW | CREATE | EDIT | DELETE | APPROVE | REJECT

### Issue: User can see data from other branch/department
1. Check user_branch_mapping entries exist
2. Check DataScopeSpecification is applied in repository query
3. Check service layer calls DataScopeUtil.getUserDataScope()
4. Verify WHERE clause includes: `branch_id IN (:allowedBranchIds) AND department_id IN (:allowedDepartmentIds)`

### Issue: UserContext is null in service
1. Verify UserInterceptor is registered in Spring config
2. Check X-user-Id header is present in request
3. Verify header value is a valid user ID that exists in DB
4. Check user record is not deleted

## Configuration

### Spring Boot Application Properties
```properties
# RBAC Configuration
rbac.enable-resource-based=true
rbac.enable-data-scope-filtering=true
rbac.deprecated-legacy-model=false

# Audit Configuration
audit.capture-user-actions=true
audit.capture-permission-denials=true
```

### Spring Security Integration (Optional)
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    // Use @PreAuthorize("@permissionChecker.hasPermission(authentication.name, 'RESOURCE_CODE', 'ACTION')")
    // on sensitive methods for declarative security
}
```

## Summary

This RBAC implementation provides:
✅ **Resource-based permissions** (vs attribute-based)
✅ **Multi-tenant data isolation** (branch/department filtering)
✅ **Dynamic permission UI** (resource matrix responses)
✅ **Scalable design** (JPA Specification pattern)
✅ **Backward compatibility** (legacy tables coexist)
✅ **Audit trail** (created_by, created_at fields)

For questions or updates, refer to the main RbacService and PermissionChecker classes.
