package ermorg.erm.constant;

/**
 * Enum for predefined RoleType codes.
 * This is optional and can be used to maintain consistency.
 * DB is not tightly coupled with this enum - custom codes can be added via database.
 */
public enum RoleTypeCode {

    SUPER_ADMIN("SUPER_ADMIN", "System / Super Admin", "Full system access"),
    ORG_ADMIN("ORG_ADMIN", "Organisation Admin", "Manages organisation/group"),
    COMPANY_ADMIN("COMPANY_ADMIN", "Company Admin", "Manages company within organisation"),
    BASIC_USER("BASIC_USER", "Basic User", "Limited access user"),
    ADVANCED_USER("ADVANCED_USER", "Advanced User", "Extended access user"),
    REPORTING_USER("REPORTING_USER", "Reporting User", "Read-only access");

    private final String code;
    private final String name;
    private final String description;

    RoleTypeCode(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValidCode(String code) {
        if (code == null) return false;

        for (RoleTypeCode rtc : values()) {
            if (rtc.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}