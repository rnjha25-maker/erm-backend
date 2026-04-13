package com.user_setup.modal;

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
    private Long priority;

//    @ManyToOne//(cascade = CascadeType.ALL)
//    private Organization organization;

//    @ManyToOne
//    private Company company;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<RoleRight> roleRights;
}
