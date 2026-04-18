package ermorg.erm.erm_command_organization.model.history;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.erm_command_organization.model.BaseModel;
import ermorg.erm.erm_command_organization.model.ModuleOrganization;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OrganizationHistory extends BaseModel {
    private Long organizationId;
    private String name;
    private String organizationLogoImageUrl;
    private String operation;
    private Integer adminCount;
	private Integer companyCount;
	private String status = "";
	private Long moduleId;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
	private List<ModuleOrganizationHistory> modules = new ArrayList<>();
	
    @ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="Plan-Id")
    private PlanHistory plan;
}
