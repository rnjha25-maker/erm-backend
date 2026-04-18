package ermorg.erm.erm_command_organization.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "user")
public class User extends BaseModel{
    private String email;
    private String password;

    @OneToOne
    private UserDetail userDetail;

    @ManyToMany
    private List<Role> roles;
    
    @ManyToOne
    @JoinColumn(name="organization_id")
    private Organization organization;
    
    @ManyToOne
    @JoinColumn(name="company_id")
    private Company compay;
    
    @ManyToOne
    @JoinColumn(name="branch_id")
    private Branch branch;
    
    @ManyToOne
    @JoinColumn(name="department_id")
    private Department department;
}
