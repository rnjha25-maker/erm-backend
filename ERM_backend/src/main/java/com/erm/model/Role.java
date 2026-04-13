package com.erm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
 

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval=true, fetch = FetchType.LAZY)
    private Set<RoleRight> roleRights;
}
