package ermorg.erm.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RiskValueUnit {
    RS("Rs."),
    THOUSANDS("Thousands"),
    LAKH("Lakh"),
    CRORES("Crores"),
    MILLION("Million"),
    BILLION("Billion"),
    TRILLION("Trillion");

    private final String label;

    RiskValueUnit(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static RiskValueUnit fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (RiskValueUnit unit : values()) {
            if (unit.name().equalsIgnoreCase(value) || unit.label.equalsIgnoreCase(value)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unsupported risk value unit: " + value);
    }
}
