package ermorg.erm.model;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="fields")
public class CustomField extends BaseModel{
	
	private String fieldName;
	private String fieldType;
	private Boolean required = false;
	private String mappedWith;
	private String fieldBehavior;
	
	private Boolean showGridColumn;
	
	private Boolean showInView;
	
	private Boolean disabled;
	
	private Integer fieldOrder;
	@OneToOne
	@JoinColumn(name="system-field-id")
	private SystemField systemField;
	@ManyToOne
	@JoinColumn(name="category_id")
	private Category category;
	
	@OneToMany(mappedBy = "field")
    private List<SubField> subFields;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(category.getId(), systemField.getId());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomField other = (CustomField) obj;
		return Objects.equals(category, other.category) && Objects.equals(systemField, other.systemField);
	}
	
	


}
