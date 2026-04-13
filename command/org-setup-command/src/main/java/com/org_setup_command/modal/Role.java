package com.org_setup_command.modal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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

    @ManyToOne//(cascade = CascadeType.ALL)
    private Organization organization;

    @ManyToOne
    private Company company;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<RoleRight> roleRights;
}
