package ermorg.erm.erm_command_organization.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ermorg.erm.erm_command_organization.model.User;
import ermorg.erm.erm_command_organization.repository.UserBranchMappingRepository;
import ermorg.erm.erm_command_organization.repository.UserDepartmentMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DataScopeUtil: Fetches and manages user data scope (branch/department access).
 * 
 * USAGE:
 * - Call from service layer before querying data
 * - Provides user's allowed branch and department IDs
 * - Integrates with DataScopeSpecification for repository queries
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataScopeUtil {

    private final UserBranchMappingRepository branchMappingRepository;
    private final UserDepartmentMappingRepository departmentMappingRepository;

    /**
     * Get all branch IDs user is allowed to access.
     * Returns primary branch if no explicit mappings exist.
     */
    public List<Long> getAllowedBranchIds(User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptyList();
        }

        // Fetch explicit branch mappings
        List<Long> mappedBranchIds = branchMappingRepository
                .findAllowedBranchIdsByUserId(user.getId());

        if (!mappedBranchIds.isEmpty()) {
            return mappedBranchIds;
        }

        // Fallback: use primary branch if set
        if (user.getBranch() != null && user.getBranch().getId() != null) {
            log.debug("No branch mapping found for user {}; using primary branch {}",
                    user.getId(), user.getBranch().getId());
            return Collections.singletonList(user.getBranch().getId());
        }

        log.warn("User {} has no branch access defined", user.getId());
        return Collections.emptyList();
    }

    /**
     * Get all department IDs user is allowed to access.
     * Returns primary department if no explicit mappings exist.
     */
    public List<Long> getAllowedDepartmentIds(User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptyList();
        }

        // Fetch explicit department mappings
        List<Long> mappedDeptIds = departmentMappingRepository
                .findAllowedDepartmentIdsByUserId(user.getId());

        if (!mappedDeptIds.isEmpty()) {
            return mappedDeptIds;
        }

        // Fallback: use primary department if set
        if (user.getDepartment() != null && user.getDepartment().getId() != null) {
            log.debug("No department mapping found for user {}; using primary department {}",
                    user.getId(), user.getDepartment().getId());
            return Collections.singletonList(user.getDepartment().getId());
        }

        log.warn("User {} has no department access defined", user.getId());
        return Collections.emptyList();
    }

    /**
     * Check if user can access a specific branch.
     */
    public boolean canAccessBranch(User user, Long branchId) {
        return getAllowedBranchIds(user).contains(branchId);
    }

    /**
     * Check if user can access a specific department.
     */
    public boolean canAccessDepartment(User user, Long departmentId) {
        return getAllowedDepartmentIds(user).contains(departmentId);
    }

    /**
     * Get combined scope: branches AND departments user can access.
     * Used for filtering queries that check both branch and department.
     */
    public DataScope getUserDataScope(User user) {
        return DataScope.builder()
                .userId(user.getId())
                .allowedBranchIds(getAllowedBranchIds(user))
                .allowedDepartmentIds(getAllowedDepartmentIds(user))
                .build();
    }

    /**
     * Value object for user's data scope.
     */
    public static class DataScope {
        public final Long userId;
        public final List<Long> allowedBranchIds;
        public final List<Long> allowedDepartmentIds;

        private DataScope(Long userId, List<Long> branchIds, List<Long> deptIds) {
            this.userId = userId;
            this.allowedBranchIds = branchIds != null ? branchIds : Collections.emptyList();
            this.allowedDepartmentIds = deptIds != null ? deptIds : Collections.emptyList();
        }

        public boolean hasBranchAccess() {
            return !allowedBranchIds.isEmpty();
        }

        public boolean hasDepartmentAccess() {
            return !allowedDepartmentIds.isEmpty();
        }

        public static DataScopeBuilder builder() {
            return new DataScopeBuilder();
        }

        public static class DataScopeBuilder {
            private Long userId;
            private List<Long> allowedBranchIds;
            private List<Long> allowedDepartmentIds;

            public DataScopeBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }

            public DataScopeBuilder allowedBranchIds(List<Long> ids) {
                this.allowedBranchIds = ids;
                return this;
            }

            public DataScopeBuilder allowedDepartmentIds(List<Long> ids) {
                this.allowedDepartmentIds = ids;
                return this;
            }

            public DataScope build() {
                return new DataScope(userId, allowedBranchIds, allowedDepartmentIds);
            }
        }
    }
}
