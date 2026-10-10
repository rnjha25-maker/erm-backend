package ermorg.erm.dashboard;

import java.util.Objects;
import org.springframework.stereotype.Component;
import ermorg.erm.constant.ErmDashboardAccessScope;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.User;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.*;

public record ChartScope(Long organizationId, Long companyId, Long creatorId,
                         Long branchId, Long departmentId, boolean restricted,
                         Long assignedBranchId, Long assignedDepartmentId) {
    @Component
    public static class Resolver {
        private final UserRepository users;
        public Resolver(UserRepository users) { this.users = users; }
        public ChartScope resolve(Long companyId, Long branchId, Long departmentId) throws ResourceNotFoundException {
            if (OrganizationContext.getOrganization() == null || UserContext.getUser() == null)
                throw new ResourceNotFoundException("Authenticated organization and user required.");
            Long orgId = OrganizationContext.getOrganization().getId();
            User user = users.findActiveByIdWithRolesAndCompany(UserContext.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
            if (orgId == null || user.getOrganization() == null || !Objects.equals(orgId, user.getOrganization().getId()))
                throw new ResourceNotFoundException("User is outside the organization.");
            for (Long id : new Long[]{companyId, branchId, departmentId})
                if (id != null && id <= 0) throw new IllegalArgumentException("Filter IDs must be positive.");
            ErmDashboardAccessScope tier = ErmDashboardRoleResolver.resolveScope(user);
            Long creator = tier == ErmDashboardAccessScope.CREATOR_ONLY ? user.getId() : null;
            boolean restricted = tier == ErmDashboardAccessScope.ADVANCED_USER_SCOPED;
            if (tier == ErmDashboardAccessScope.COMPANY_SCOPED || restricted) {
                if (user.getCompany() == null || user.getCompany().getId() == null)
                    throw new ResourceNotFoundException("User company required.");
                Long assigned = user.getCompany().getId();
                if (companyId != null && !Objects.equals(companyId, assigned))
                    throw new ResourceNotFoundException("Company outside user scope.");
                companyId = assigned;
            }
            var assignedBranches = ErmDashboardRoleResolver.resolveAssignedBranchIds(user);
            var assignedDepartments = ErmDashboardRoleResolver.resolveAssignedDepartmentIds(user);
            return new ChartScope(orgId, companyId, creator, branchId, departmentId, restricted,
                assignedBranches.isEmpty() ? null : assignedBranches.get(0),
                assignedDepartments.isEmpty() ? null : assignedDepartments.get(0));
        }
    }
}
