package ermorg.erm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "system_fields")
@ToString
public class SystemField extends BaseModel {

    /** Java property name on the response DTO — used for reflection/strategy lookup */
    private String field;

    /** Human-readable label shown in UI (e.g. "Risk Appetite Status") */
    @Column(name = "display_label")
    private String displayLabel;

    /**
     * Canonical field type: TEXT, NUMBER, DATE, DROPDOWN, MULTI_SELECT,
     * CURRENCY, BOOLEAN, DATE_PICKER, INPUT_FIELD, TEXT_AREA
     */
    @Column(name = "field_type")
    private String fieldType;

    /** Default value rendered when no data is present */
    @Column(name = "default_value")
    private String defaultValue;

    /** JSON validation rules, e.g. {"min":0,"max":100,"pattern":"..."} */
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules;

    /** Whether this field is mandatory by default */
    @Column(name = "is_required")
    private Boolean isRequired = false;

    /** Whether this field is read-only by default */
    @Column(name = "is_read_only")
    private Boolean isReadOnly = false;

    /** Whether this field is hidden by default */
    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    /** Default display order within its system table */
    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne
    @JoinColumn(name = "system_table_id")
    private SystemTable systemTable;
}
