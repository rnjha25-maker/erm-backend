package com.erm.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Modules extends BaseModel{

	private String name;
	private String description;
	@OneToMany(mappedBy="module", cascade = CascadeType.ALL)
	private List<Right> rights = new ArrayList<>();
	
	@OneToMany(mappedBy="module", cascade = CascadeType.ALL)
	private List<Category> categories = new ArrayList<>();
	
	@OneToMany(mappedBy="module", cascade = CascadeType.ALL)
	private List<ViewCategory> viewCategories = new ArrayList<>();
}
