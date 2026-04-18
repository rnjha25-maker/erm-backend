package ermorg.erm.erm_query_organization.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Company extends BaseModel{
    private String name;
    private String companyLogoImageUrl;

    @OneToMany(mappedBy = "company")
    private List<Branch> branches;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}
