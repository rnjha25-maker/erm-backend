package ermorg.erm.erm_api_gateway.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "category")
@ToString
public class Category extends BaseModel {

	@Column(name = "category_name")
	private String categoryName;

	@Column(name = "mapped_with_table")
	private String mappedWithTable;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CustomField> fields = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "module_id")
	private Modules module;

	public Category(String categoryName, String mappedWithTable) {
		super();
		this.categoryName = categoryName;
		this.mappedWithTable = mappedWithTable;
	}
}
