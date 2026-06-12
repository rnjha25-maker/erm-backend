package ermorg.erm.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import ermorg.erm.constant.ErmDashboardAccessScope;
import ermorg.erm.constant.RoleTypeCode;
import ermorg.erm.model.Role;
import ermorg.erm.model.User;

/**
 * Determines erm-summary data scope from the user's {@link Role} names ({@link Role#getName()}).
 * Precedence when multiple tiers match: organization admin wins, then company/advance, then basic.
 */
public final class ErmDashboardRoleResolver {


	private static final Set<String> ORG_ADMIN_NAMES_NORMALIZED = Set.of("org admin", "organization admin");

	private static final Set<String> COMPANY_OR_ADVANCED_NAMES_NORMALIZED = Set.of("company admin", "cmp admin",
			"advance user", "advanced user");

	private static final Set<String> BASIC_USER_NAMES_NORMALIZED = Set.of("basic user");

	private ErmDashboardRoleResolver() {
	}

	static String normalizeRoleName(String name) {
		if (name == null) {
			return "";
		}
		String lower = name.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
		return lower.replaceAll("\\s+", " ");
	}

	public static ErmDashboardAccessScope resolveScope(User user) throws IllegalArgumentException {
		List<Role> roles = user.getRoles();
		if (roles == null || roles.isEmpty()) {
			throw new IllegalArgumentException(
					"No application role found for dashboard access. Assign Org admin, Company/Advance admin, or Basic user.");
		}
		boolean orgAdmin = false;
		boolean companyOrAdvance = false;
		boolean basic = false;

		for (Role role : roles) {
			if (role == null) {
				continue;
			}
			RoleTypeCode roleTypeCode = RoleTypeCode.valueOf(role.getRoleType().getName());
			String n = normalizeRoleName(role.getName());
			if (n.isEmpty()) {
				continue;
			}
			if (roleTypeCode == RoleTypeCode.ADMIN) {
				orgAdmin = true;
			} else if (roleTypeCode == RoleTypeCode.COMPANY) {
				companyOrAdvance = true;
			} else if (roleTypeCode == RoleTypeCode.BUSINESS) {
				basic = true;
			}
		}

		if (orgAdmin) {
			return ErmDashboardAccessScope.ORGANIZATION_WIDE;
		}
		if (companyOrAdvance) {
			return ErmDashboardAccessScope.COMPANY_SCOPED;
		}
		if (basic) {
			return ErmDashboardAccessScope.CREATOR_ONLY;
		}
		throw new IllegalArgumentException(
				"No recognized dashboard role. Expected role name among: Organization admin, Company admin, Advance/Advanced user, Basic user (or org_admin, cmp_admin, basic_user).");
	}
}
