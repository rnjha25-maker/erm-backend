package com.erm.erm_query_organization.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "rights")
public class Right extends BaseModel{
    private String name;
    private String description;
    
    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name="organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "right", cascade = CascadeType.ALL)
    private Set<RoleRight> roleRights;
}
