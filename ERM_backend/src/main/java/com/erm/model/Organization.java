package com.erm.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Organization extends BaseModel {
    private String name;
    private String organizationLogoImageUrl;

	private String description;
	private String pinCode;
	private String businessLocation;
	private Integer adminCount;
	private Integer companyCount;
    private String status = "ACTIVE";
    private String gstNo;
    private String panNo;
    private Long noOfBasicUsers;
    private Long noOfAdvancedUsers;
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Company> companies = new ArrayList<>();
    
//    @OneToMany(cascade=CascadeType.ALL, mappedBy="organization", orphanRemoval = true)
//    private List<RoleRight> roleRight;
    
    @OneToMany(mappedBy="organization", fetch = FetchType.LAZY)
    private List<Department> departments = new ArrayList<>();
//    @OneToMany(mappedBy="organization")
//    private List<CustomFields> fields;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ModuleOrganization> modules = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name="country-id")
    private Country country;
    
    @ManyToOne
    @JoinColumn(name="city-id")
    private City city;
    
    @ManyToOne
    @JoinColumn(name="state-id")
    private State state;
    
    @ManyToOne
	@JoinColumn(name="Plan-Id")
	private Plan plan;
    
	@ManyToOne
	@JoinColumn(name="POC-Person-Id")
    private UserDetail POCPerson;
	
	@OneToMany
	private List<Right> rights = new ArrayList<>();
}
