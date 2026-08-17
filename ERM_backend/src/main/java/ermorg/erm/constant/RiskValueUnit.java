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
        // Normalise common singular/plural variants sent by different frontend versions.
        // E.g. "Crore" → "Crores", "crore" → "Crores", "lakh" → "Lakh", "rs" → "Rs."
        String trimmed = value.trim();
        for (RiskValueUnit unit : values()) {
            if (unit.name().equalsIgnoreCase(trimmed) || unit.label.equalsIgnoreCase(trimmed)) {
                return unit;
            }
        }
        // Secondary alias table for common mis-spellings / singular forms
        String normalised = trimmed.toLowerCase();
        if (normalised.equals("crore"))                          return CRORES;
        if (normalised.equals("million dollar"))                 return MILLION;
        if (normalised.equals("rs") || normalised.equals("rs.") || normalised.equals("rupee") || normalised.equals("rupees")) return RS;
        if (normalised.equals("thousand") || normalised.equals("k")) return THOUSANDS;
        if (normalised.equals("lac"))                            return LAKH;
        if (normalised.equals("bn"))                             return BILLION;
        if (normalised.equals("tn"))                             return TRILLION;

        throw new IllegalArgumentException("Unsupported risk value unit: " + value);
    }
}
