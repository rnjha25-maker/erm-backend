package com.erm.erm_query_organization.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Organization extends BaseModel {
    private String name;
    private String organizationLogoImageUrl;

    @OneToMany(mappedBy = "organization")
    private List<Company> companies;
    
    @OneToMany(cascade=CascadeType.MERGE, mappedBy="organization")
    private List<RoleRight> roleRight;
}
