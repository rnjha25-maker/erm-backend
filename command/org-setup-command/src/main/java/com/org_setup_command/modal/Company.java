package com.org_setup_command.modal;

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
public class Company extends BaseModel{
    private String name;
    private String companyLogoImageUrl;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Branch> branches = new ArrayList<>();

    @ManyToOne//(cascade=CascadeType.MERGE)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    @OneToMany
    private List<Modules> modules;
}
