package ermorg.erm.constant;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RiskAcceptanceLevel {
    ACCEPTABLE_RISK,
    ACCEPTABLE_WITH_MITIGATION,
    ACCEPTABLE_WITH_MONITORING,
    ACCEPTABLE_WITHOUT_MITIGATION_MONITORING,
    UNACCEPTABLE_RISK;

    @JsonCreator
    public static RiskAcceptanceLevel fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalize(value);
        for (RiskAcceptanceLevel level : values()) {
            if (normalize(level.name()).equals(normalized)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unsupported risk acceptance level: " + value);
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
