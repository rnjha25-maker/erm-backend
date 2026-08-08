package ermorg.erm.dto.response;

import ermorg.erm.model.FieldOption;
import ermorg.erm.model.SystemField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class SystemFieldResponse {

    private Long id;
    private String field;
    private String displayLabel;
    private String fieldType;
    private String defaultValue;
    private String validationRules;
    private Boolean isRequired;
    private Boolean isReadOnly;
    private Boolean isHidden;
    private Integer displayOrder;
    private List<FieldOptionResponse> options = Collections.emptyList();

    public SystemFieldResponse(SystemField systemField) {
        this.id           = systemField.getId();
        this.field        = systemField.getField();
        this.displayLabel = systemField.getDisplayLabel() != null
                ? systemField.getDisplayLabel()
                : systemField.getField();
        this.fieldType       = systemField.getFieldType();
        this.defaultValue    = systemField.getDefaultValue();
        this.validationRules = systemField.getValidationRules();
        this.isRequired      = Boolean.TRUE.equals(systemField.getIsRequired());
        this.isReadOnly      = Boolean.TRUE.equals(systemField.getIsReadOnly());
        this.isHidden        = Boolean.TRUE.equals(systemField.getIsHidden());
        this.displayOrder    = systemField.getDisplayOrder();
    }

    public SystemFieldResponse(SystemField systemField, List<FieldOption> fieldOptions) {
        this(systemField);
        if (fieldOptions != null && !fieldOptions.isEmpty()) {
            this.options = fieldOptions.stream()
                    .map(FieldOptionResponse::new)
                    .toList();
        }
    }

    @Data
    @NoArgsConstructor
    public static class FieldOptionResponse {
        private Long    id;
        private String  value;
        private String  label;
        private Integer displayOrder;

        public FieldOptionResponse(FieldOption option) {
            this.id           = option.getId();
            this.value        = option.getOptionValue();
            this.label        = option.getOptionLabel();
            this.displayOrder = option.getDisplayOrder();
        }
    }
}
