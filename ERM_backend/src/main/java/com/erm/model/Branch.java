package com.erm.model;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Branch extends BaseModel{
    private String name;
    private String description;
    private String type;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;
    
    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;
    
    @ManyToOne
	@JoinColumn(name = "city_id")
    private City city;
    
    private String pincode;
	private String address;
	private String status;
    @OneToMany(mappedBy = "branch")
    private List<Department> departments = new ArrayList<>();

    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="branch", orphanRemoval = true)
    private List<UserDetail> users = new ArrayList<>();
    
    @ManyToOne//(cascade = CascadeType.MERGE)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}
