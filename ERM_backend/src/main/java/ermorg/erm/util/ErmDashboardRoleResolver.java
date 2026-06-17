package ermorg.erm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ermorg.erm.constant.ErmDashboardAccessScope;
import ermorg.erm.constant.RoleTypeCode;
import ermorg.erm.model.Branch;
import ermorg.erm.model.Department;
import ermorg.erm.model.Role;
import ermorg.erm.model.User;

/**
 * Determines erm-summary data scope from the user's {@link Role} types.
 * Precedence when multiple tiers match: org admin, then company admin, then advanced user, then basic.
 */
public final class ErmDashboardRoleResolver {

	private ErmDashboardRoleResolver() {
	}

	public static ErmDashboardAccessScope resolveScope(User user) throws IllegalArgumentException {
		List<Role> roles = user.getRoles();
		if (roles == null || roles.isEmpty()) {
			throw new IllegalArgumentException(
					"No application role found for dashboard access. Assign Org admin, Company admin, Advanced user, or Basic user.");
		}
		boolean orgAdmin = false;
		boolean companyAdmin = false;
		boolean advancedUser = false;
		boolean basic = false;

		for (Role role : roles) {
			if (role == null) {
				continue;
			}
			RoleTypeCode roleTypeCode = resolveRoleTypeCode(role);
			if (roleTypeCode == null) {
				continue;
			}
			switch (roleTypeCode) {
			case ORG_ADMIN:
				orgAdmin = true;
				break;
			case COMPANY_ADMIN:
				companyAdmin = true;
				break;
			case ADVANCED_USER:
				advancedUser = true;
				break;
			case BASIC_USER:
				basic = true;
				break;
			default:
				break;
			}
		}

		if (orgAdmin) {
			return ErmDashboardAccessScope.ORGANIZATION_WIDE;
		}
		if (companyAdmin) {
			return ErmDashboardAccessScope.COMPANY_SCOPED;
		}
		if (advancedUser) {
			return ErmDashboardAccessScope.ADVANCED_USER_SCOPED;
		}
		if (basic) {
			return ErmDashboardAccessScope.CREATOR_ONLY;
		}
		throw new IllegalArgumentException(
				"No recognized dashboard role. Expected Org admin, Company admin, Advanced user, or Basic user.");
	}

	public static List<Long> resolveAssignedBranchIds(User user) {
		if (user == null) {
			return Collections.emptyList();
		}
		List<Long> ids = new ArrayList<>();
		Branch branch = user.getBranch();
		if (branch != null) {
			ids.add(branch.getId());
		}
		return ids;
	}

	public static List<Long> resolveAssignedDepartmentIds(User user) {
		if (user == null) {
			return Collections.emptyList();
		}
		List<Long> ids = new ArrayList<>();
		Department department = user.getDepartment();
		if (department != null) {
			ids.add(department.getId());
		}
		return ids;
	}

	private static RoleTypeCode resolveRoleTypeCode(Role role) {
		if (role.getRoleType() == null || role.getRoleType().getCode() == null) {
			return null;
		}
		try {
			return RoleTypeCode.valueOf(role.getRoleType().getCode());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
