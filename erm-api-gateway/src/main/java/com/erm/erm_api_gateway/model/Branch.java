package com.erm.erm_api_gateway.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Branch extends BaseModel {
	private String name;
	private String description;
	private String type;

	@OneToMany
	private List<Department> departments;

	@OneToOne
	private Address address;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "branch", orphanRemoval = true)
	private List<UserDetail> users;

	@ManyToOne // (cascade = CascadeType.MERGE)
	@JoinColumn(name = "company_id")
	private Company company;
}
