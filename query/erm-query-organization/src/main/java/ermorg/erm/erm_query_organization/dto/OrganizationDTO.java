package ermorg.erm.erm_query_organization.dto;

import ermorg.erm.erm_query_organization.model.Organization;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OrganizationDTO {
    private Long id;
    private String name;
    private String organizationLogoImageUrl;
    private int companyCount;
    private Date createdAt;

    public OrganizationDTO(Organization organization) {
        this.id = organization.getId();
        this.name = organization.getName();
        this.organizationLogoImageUrl = organization.getOrganizationLogoImageUrl();
        this.companyCount = organization.getCompanies().size();
        this.createdAt = organization.getCreatedAt();
    }
}
