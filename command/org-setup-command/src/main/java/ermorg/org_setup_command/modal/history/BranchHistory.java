package ermorg.org_setup_command.modal.history;

import ermorg.org_setup_command.modal.BaseModel;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BranchHistory extends BaseModel {
    private Long branchId;
    private String name;
    private String description;
    private String type;
    private String operation;
}
