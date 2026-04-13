package com.erm.erm_query_organization.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Branch extends BaseModel{
    private String name;
    private String description;
    private String type;

    @OneToMany(mappedBy = "branch")
    private List<Department> departments;

    @OneToOne
    private Address address;

    @OneToMany(cascade=CascadeType.MERGE, mappedBy="branch")
    private List<UserDetail> users;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}
