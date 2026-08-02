package ermorg.erm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores selectable options for DROPDOWN and MULTI_SELECT system fields.
 *
 * Example rows:
 *   systemField.field = "valueUnit", optionValue = "RS",       optionLabel = "Rs."
 *   systemField.field = "valueUnit", optionValue = "THOUSANDS", optionLabel = "Thousands"
 *
 * Adding a new dropdown option = one DB INSERT, zero Java changes.
 */
@Getter
@Setter
@Entity
@Table(
    name = "field_options",
    indexes = {
        @Index(name = "idx_field_options_sf_deleted", columnList = "system_field_id, deleted"),
        @Index(name = "idx_field_options_sf_order",   columnList = "system_field_id, display_order")
    }
)
public class FieldOption extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_field_id", nullable = false)
    private SystemField systemField;

    /** Stored value persisted to the entity column (e.g. "RS", "MONTHLY") */
    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    /** Human-readable label shown in the UI (e.g. "Rs.", "Monthly") */
    @Column(name = "option_label", nullable = false, length = 255)
    private String optionLabel;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
