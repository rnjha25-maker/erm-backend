package ermorg.erm.dto.response;

import ermorg.erm.model.CustomField;
import ermorg.erm.model.FieldOption;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class CustomFieldResponse {

    private Long    id;
    private String  fieldName;
    private String  fieldType;
    private Boolean required = false;
    private String  systemFieldName;
    private String  fieldBehavior;
    private Boolean showGridColumn;
    private Boolean showInView;
    private Boolean disabled;
    private Integer fieldOrder;
    private String  value;

    /** Dropdown / multi-select options — populated when the system field has options configured */
    private List<FieldOptionItem> options = Collections.emptyList();

    public CustomFieldResponse(CustomField customField) {
        this.id             = customField.getId();
        this.fieldName      = customField.getFieldName();
        this.fieldType      = customField.getFieldType();
        this.required       = customField.getRequired();
        this.systemFieldName = customField.getSystemField().getField();
        this.fieldBehavior  = customField.getFieldBehavior();
        this.showGridColumn = customField.getShowGridColumn();
        this.showInView     = customField.getShowInView();
        this.disabled       = customField.getDisabled();
        this.fieldOrder     = customField.getFieldOrder();
    }

    /** Enrich this response with dropdown options loaded from field_options table */
    public void setOptionsFromFieldOptions(List<FieldOption> fieldOptions) {
        if (fieldOptions == null || fieldOptions.isEmpty()) {
            this.options = Collections.emptyList();
            return;
        }
        this.options = fieldOptions.stream()
                .map(fo -> new FieldOptionItem(fo.getOptionValue(), fo.getOptionLabel(), fo.getDisplayOrder()))
                .toList();
    }

    @Data
    @NoArgsConstructor
    public static class FieldOptionItem {
        private String  value;
        private String  label;
        private Integer displayOrder;

        public FieldOptionItem(String value, String label, Integer displayOrder) {
            this.value        = value;
            this.label        = label;
            this.displayOrder = displayOrder;
        }
    }
}
