package ermorg.erm.erm_command_organization.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ermorg.erm.erm_command_organization.model.PermissionAction;
import ermorg.erm.erm_command_organization.model.User;
import ermorg.erm.erm_command_organization.util.DataScopeSpecification;
import ermorg.erm.erm_command_organization.util.DataScopeUtil;
import ermorg.erm.erm_command_organization.util.PermissionChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * BaseSecureService: Abstract base for all services with data access enforcement.
 * 
 * PATTERN: Subclasses use:
 * 1. enforcePermission() - Check action permission
 * 2. getUserDataScope() - Get branch/department scope
 * 3. getDataScopedSpecification() - Create Specification for repository query
 * 
 * EXAMPLE:
 * ```
 * List<RiskIdentification> risks = repository.findAll(
 *     getDataScopedSpecification(user)
 * );
 * ```
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BaseSecureService {

    protected final PermissionChecker permissionChecker;
    protected final DataScopeUtil dataScopeUtil;

    /**
     * Enforce action permission: throw exception if denied.
     * @throws AccessDeniedException if permission not granted
     */
    protected void enforcePermission(Long userId, String resourceCode, PermissionAction action) {
        boolean hasPermission = permissionChecker.hasPermission(userId, resourceCode, action);
        
        if (!hasPermission) {
            log.warn("Permission denied: userId={}, resource={}, action={}", 
                userId, resourceCode, action);
            throw new AccessDeniedException(
                String.format("User %d is not authorized for action %s on resource %s", 
                    userId, action, resourceCode)
            );
        }
        
        log.debug("Permission granted: userId={}, resource={}, action={}", 
            userId, resourceCode, action);
    }

    /**
     * Get user's data scope (branch + department).
     * Returns empty lists if user has no scope defined.
     */
    protected DataScopeUtil.DataScope getUserDataScope(User user) {
        return dataScopeUtil.getUserDataScope(user);
    }

    /**
     * Create JPA Specification for data scope filtering.
     * Combines branch AND department filters.
     * Usage: repository.findAll(getDataScopedSpecification(user))
     */
    protected <T> Specification<T> getDataScopedSpecification(User user) {
        DataScopeUtil.DataScope scope = getUserDataScope(user);
        
        if (!scope.hasBranchAccess() && !scope.hasDepartmentAccess()) {
            log.warn("User {} has no data scope defined", user.getId());
            // Return specification that matches nothing (deny all)
            return (root, query, cb) -> cb.or();
        }
        
        return DataScopeSpecification.fromDataScope(scope);
    }

    /**
     * Combine data scope with additional filters.
     * Usage: repository.findAll(
     *     getDataScopedSpecification(user)
     *     .and(otherFilters)
     * )
     */
    protected <T> Specification<T> getDataScopedSpecification(User user, Specification<T> additionalFilters) {
        return getDataScopedSpecification(user).and(additionalFilters);
    }

    /**
     * Helper: Check if user can access a specific branch.
     */
    protected boolean canAccessBranch(User user, Long branchId) {
        return dataScopeUtil.canAccessBranch(user, branchId);
    }

    /**
     * Helper: Check if user can access a specific department.
     */
    protected boolean canAccessDepartment(User user, Long departmentId) {
        return dataScopeUtil.canAccessDepartment(user, departmentId);
    }

    /**
     * Log permission check result.
     */
    protected void logPermissionCheck(Long userId, String resource, PermissionAction action, boolean result) {
        String status = result ? "GRANTED" : "DENIED";
        log.info("Permission check [{}]: userId={}, resource={}, action={}", 
            status, userId, resource, action);
    }

    /**
     * Custom exception for access control.
     */
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }

        public AccessDeniedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
