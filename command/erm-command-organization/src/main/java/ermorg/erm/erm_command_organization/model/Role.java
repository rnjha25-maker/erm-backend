package ermorg.erm.erm_command_organization.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "role", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_code", columnNames = "role_code")
})
public class Role extends BaseModel {
    @Column(name = "role_code", length = 80)
    private String roleCode;

    private String name;

    @Column(name = "normalized_name", length = 150)
    private String normalizedName;

    private String description;
    private Long priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 30)
    private RoleType roleType = RoleType.CUSTOM;

    private Boolean active = true;

//    @ManyToOne//(cascade = CascadeType.ALL)
//    private Organization organization;

//    @ManyToOne
//    private Company company;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<RoleRight> roleRights;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RolePermission> rolePermissions;
}
