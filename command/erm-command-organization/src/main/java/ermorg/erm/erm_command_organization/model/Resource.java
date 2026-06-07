package ermorg.erm.erm_command_organization.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
@Table(name = "resources", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resource_code", columnNames = "resource_code")
})
public class Resource extends BaseModel {

    @Column(name = "resource_code", nullable = false, length = 120)
    private String resourceCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType = ResourceType.PAGE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_resource_id")
    private Resource parentResource;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean active = true;

    @ElementCollection(targetClass = PermissionAction.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "resource_actions", joinColumns = @JoinColumn(name = "resource_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private Set<PermissionAction> supportedActions = new HashSet<>();
}
