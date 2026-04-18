package ermorg.user_setup.util;

import ermorg.user_setup.modal.Organization;


public class OrganizationContext {

    private static final ThreadLocal<Organization> organizationThreadLocal = new ThreadLocal<>();

    public static void setOrganization(Organization organization) {
        organizationThreadLocal.set(organization);
    }

    public static Organization getOrganization() {
        return organizationThreadLocal.get();
    }

    public static void clear() {
        organizationThreadLocal.remove();
    }
}
