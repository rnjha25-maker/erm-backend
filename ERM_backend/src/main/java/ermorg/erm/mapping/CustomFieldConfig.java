package ermorg.erm.mapping;

import ermorg.erm.dto.response.CustomFieldResponse;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomFieldConfig {

    private String fieldName;
    private String systemFieldName;
    private String fieldType;

    public CustomFieldConfig(CustomFieldResponse response) {
        if (response == null) {
            return;
        }
        this.fieldName = response.getFieldName();
        this.systemFieldName = response.getSystemFieldName();
        this.fieldType = response.getFieldType();
    }

    public String normalizedKey() {
        String key = systemFieldName != null && !systemFieldName.trim().isEmpty() ? systemFieldName : fieldName;
        return normalizeKey(key);
    }

    public static String normalizeKey(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
