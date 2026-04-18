package ermorg.org_setup_command.modal.history;

import ermorg.org_setup_command.modal.BaseModel;

import jakarta.persistence.Entity;
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
}
