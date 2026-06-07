package ermorg.erm.erm_command_organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role_permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_resource_action", columnNames = {"role_id", "resource_id", "action"})
})
public class RolePermission extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PermissionAction action;

    @Column(nullable = false)
    private Boolean allowed = true;

    public static RolePermission allowed(Role role, Resource resource, PermissionAction action) {
        RolePermission permission = new RolePermission();
        permission.setRole(role);
        permission.setResource(resource);
        permission.setAction(action);
        permission.setAllowed(true);
        permission.setDeleted(false);
        return permission;
    }
}
