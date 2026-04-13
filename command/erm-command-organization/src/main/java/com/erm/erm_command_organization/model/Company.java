package com.erm.erm_command_organization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Company extends BaseModel{
    private String name;
    private String companyLogoImageUrl;
    private String companySite;
    private String pincode;
    private String address;
    private String status;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Branch> branches;

    @ManyToOne//(cascade=CascadeType.MERGE)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    @ManyToOne
    @JoinColumn(name="country-id")
    private Country country;
    
    @ManyToOne
    @JoinColumn(name="city-id")
    private City city;
    
    @ManyToOne
    @JoinColumn(name="state-id")
    private State state;
}
