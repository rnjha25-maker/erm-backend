package com.erm.erm_query_organization.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private Set<RoleRight> roleRights;
}
