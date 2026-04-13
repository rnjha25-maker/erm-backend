package com.org_setup_command.util;

import com.org_setup_command.modal.Organization;


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
