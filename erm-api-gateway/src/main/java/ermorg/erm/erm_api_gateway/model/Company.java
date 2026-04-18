package ermorg.erm.erm_api_gateway.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Company extends BaseModel {
	private String name;
	private String companyLogoImageUrl;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Branch> branches;

	@ManyToOne // (cascade=CascadeType.MERGE)
	@JoinColumn(name = "organization_id")
	private Organization organization;
}
