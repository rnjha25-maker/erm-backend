package ermorg.erm.erm_command_organization.constant;

/**
 * Enum for predefined RoleType codes.
 * This is optional and can be used to maintain consistency.
 * DB is not tightly coupled with this enum - custom codes can be added via database.
 */
public enum RoleTypeCode {
    
    SYSTEM("SYSTEM", "System Role Type", "Built-in system roles"),
    BUSINESS("BUSINESS", "Business Role Type", "Business domain roles"),
    CUSTOM("CUSTOM", "Custom Role Type", "Custom organization roles"),
    ADMIN("ADMIN", "Administrator Role Type", "Administrative roles");
    
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
        if (code == null) {
            return false;
        }
        for (RoleTypeCode rtc : RoleTypeCode.values()) {
            if (rtc.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
