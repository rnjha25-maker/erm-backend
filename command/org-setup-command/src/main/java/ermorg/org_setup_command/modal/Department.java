package ermorg.org_setup_command.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Department extends BaseModel{
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
