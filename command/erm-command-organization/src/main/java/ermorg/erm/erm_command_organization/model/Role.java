package ermorg.erm.erm_command_organization.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
public class Role extends BaseModel {
    private String name;
    private String description;
    private Long priority;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_type_id", nullable = false)
    private RoleType roleType;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<RoleRight> roleRights;
}
