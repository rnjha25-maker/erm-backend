package ermorg.erm.erm_command_organization.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.erm_command_organization.dto.ResourceWithPermissionsResponse;
import ermorg.erm.erm_command_organization.model.PermissionAction;
import ermorg.erm.erm_command_organization.model.Resource;
import ermorg.erm.erm_command_organization.model.RolePermission;
import ermorg.erm.erm_command_organization.model.User;
import ermorg.erm.erm_command_organization.repository.RolePermissionRepository;
import ermorg.erm.erm_command_organization.repository.ResourceRepository;
import ermorg.erm.erm_command_organization.repository.UserRepository;
import ermorg.erm.erm_command_organization.util.DataScopeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RbacService: Permission and authorization service.
 * 
 * RESPONSIBILITIES:
 * 1. Check user permissions for resources and actions
 * 2. Return UI-friendly permission matrices
 * 3. Retrieve accessible resources for a user
 * 4. Validate data scope (branch/department)
 * 
 * USAGE IN CONTROLLERS:
 * ```java
 * @GetMapping("/api/rbac/user-resources")
 * public List<ResourceWithPermissionsResponse> getUserResources(@RequestHeader("X-user-Id") Long userId) {
 *     return rbacService.getUserAccessibleResources(userId);
 * }
 * ```
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RbacService extends BaseSecureService {

    private final ResourceRepository resourceRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;

    // ============================================================================
    // Permission Checks (Core)
    // ============================================================================

    /**
     * Check if user has permission to perform action on resource.
     * Returns boolean (doesn't throw exception).
     */
    public boolean userHasPermission(Long userId, String resourceCode, PermissionAction action) {
        boolean result = rolePermissionRepository.existsAllowedPermission(
            userId, 
            resourceCode.toUpperCase(), 
            action
        );
        
        logPermissionCheck(userId, resourceCode, action, result);
        return result;
    }

    /**
     * Require permission: throws exception if denied.
     * Use before sensitive operations.
     */
    public void requireUserPermission(Long userId, String resourceCode, PermissionAction action) {
        enforcePermission(userId, resourceCode, action);
    }

    // ============================================================================
    // Resource Access (For UI Navigation)
    // ============================================================================

    /**
     * Get all resources accessible to user with permission flags.
     * Suitable for dynamic menu construction or permission matrix display.
     * 
     * RESPONSE:
     * [
     *   {
     *     "resourceCode": "RISK_IDENTIFICATION",
     *     "resourceName": "Risk Identification",
     *     "permissions": {
     *       "VIEW": true,
     *       "CREATE": true,
     *       "EDIT": false,
     *       "DELETE": false
     *     }
     *   },
     *   ...
     * ]
     */
    public List<ResourceWithPermissionsResponse> getUserAccessibleResources(Long userId) {
        log.info("Fetching accessible resources for user {}", userId);
        
        User user = userRepository.findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Get all active resources
        List<Resource> allResources = resourceRepository
            .findByActiveTrue()
            .stream()
            .filter(r -> !r.isDeleted())
            .collect(Collectors.toList());

        // Map to response DTOs with permissions
        return allResources.stream()
            .map(resource -> buildResourceWithPermissions(user, resource))
            .filter(ResourceWithPermissionsResponse::hasAnyPermission)
            .collect(Collectors.toList());
    }

    /**
     * Get resources by type with permissions.
     * Example: Get all PAGE type resources.
     */
    public List<ResourceWithPermissionsResponse> getUserAccessibleResourcesByType(
            Long userId, String resourceType) {
        log.info("Fetching {} resources for user {}", resourceType, userId);
        
        User user = userRepository.findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow();

        List<RolePermission> userPermissions = rolePermissionRepository
            .findAllowedByUserIdAndResourceType(userId, resourceType);

        return userPermissions.stream()
            .map(RolePermission::getResource)
            .distinct()
            .map(resource -> buildResourceWithPermissions(user, resource))
            .collect(Collectors.toList());
    }

    /**
     * Get permissions for a single resource.
     */
    public ResourceWithPermissionsResponse getUserPermissionsForResource(
            Long userId, String resourceCode) {
        log.info("Fetching permissions for user {} on resource {}", userId, resourceCode);
        
        User user = userRepository.findById(userId).orElseThrow();
        Resource resource = resourceRepository.findByResourceCodeAndDeletedFalse(resourceCode)
            .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceCode));

        return buildResourceWithPermissions(user, resource);
    }

    // ============================================================================
    // Data Scope Validation
    // ============================================================================

    /**
     * Validate user can access data in a specific branch.
     * Use before querying branch-specific data.
     */
    public void validateBranchAccess(Long userId, Long branchId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        if (!canAccessBranch(user, branchId)) {
            log.warn("Branch access denied: userId={}, branchId={}", userId, branchId);
            throw new BaseSecureService.AccessDeniedException(
                String.format("User %d cannot access branch %d", userId, branchId)
            );
        }
    }

    /**
     * Validate user can access data in a specific department.
     * Use before querying department-specific data.
     */
    public void validateDepartmentAccess(Long userId, Long departmentId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        if (!canAccessDepartment(user, departmentId)) {
            log.warn("Department access denied: userId={}, departmentId={}", userId, departmentId);
            throw new BaseSecureService.AccessDeniedException(
                String.format("User %d cannot access department %d", userId, departmentId)
            );
        }
    }

    // ============================================================================
    // Helper Methods (Private)
    // ============================================================================

    /**
     * Build ResourceWithPermissionsResponse DTO from entity and user permissions.
     */
    private ResourceWithPermissionsResponse buildResourceWithPermissions(User user, Resource resource) {
        ResourceWithPermissionsResponse response = new ResourceWithPermissionsResponse();
        
        response.setResourceId(resource.getId());
        response.setResourceCode(resource.getResourceCode());
        response.setResourceName(resource.getName());
        response.setResourceType(resource.getResourceType() != null ? 
            resource.getResourceType().toString() : null);
        response.setDisplayOrder(resource.getDisplayOrder());

        // Populate permission flags for all supported actions
        if (resource.getSupportedActions() != null && !resource.getSupportedActions().isEmpty()) {
            resource.getSupportedActions().forEach(action -> {
                boolean hasPermission = rolePermissionRepository.existsAllowedPermission(
                    user.getId(),
                    resource.getResourceCode(),
                    action
                );
                response.setPermission(action.toString(), hasPermission);
            });
        }

        return response;
    }

    // ============================================================================
    // Audit & Logging
    // ============================================================================

    /**
     * Log permission denied event for audit trail.
     * Can be used for security monitoring and suspicious activity detection.
     */
    public void logPermissionDenied(Long userId, String resource, PermissionAction action) {
        log.warn("PERMISSION_DENIED: userId={}, resource={}, action={}, timestamp={}", 
            userId, resource, action, System.currentTimeMillis());
        // TODO: Send to audit service, security dashboard, etc.
    }

    /**
     * Log permission granted for audit trail (optional, verbose).
     */
    public void logPermissionGranted(Long userId, String resource, PermissionAction action) {
        log.debug("PERMISSION_GRANTED: userId={}, resource={}, action={}", 
            userId, resource, action);
    }
}
