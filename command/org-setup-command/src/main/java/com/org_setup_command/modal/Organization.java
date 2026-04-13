package com.org_setup_command.modal;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Organization extends BaseModel {
    private String name;
    private String organizationLogoImageUrl;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Company> companies;
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="organization", orphanRemoval = true)
    private List<RoleRight> roleRight;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    private List<ModuleOrganization> modules = new ArrayList<>();
    
//    @OneToMany(mappedBy="organization")
//    private List<Fields> fields;
}
