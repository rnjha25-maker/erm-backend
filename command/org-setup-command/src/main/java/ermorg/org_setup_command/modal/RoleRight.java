package ermorg.org_setup_command.modal;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RoleRight extends BaseModel{
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "right_id")
    private Right right;

//    @Enumerated(EnumType.ORDINAL)
//    private Permission permission;
    
    @Column(name="created_permission")
    private Boolean create;

    @Column(name="update_permission")
    private Boolean update;

    @Column(name="delete_permission")
    private Boolean delete;
    
    @OneToMany//(cascade=CascadeType.MERGE)
    private List<Right> rights;
    
    @ManyToOne//(cascade=CascadeType.MERGE)
    @JoinColumn(name="organization_id")
    private Organization organization;
}
