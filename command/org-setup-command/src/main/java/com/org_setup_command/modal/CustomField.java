package com.org_setup_command.modal;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="fields")
public class CustomField extends BaseModel{
	
	private String fieldName;
	private String fieldType;
	private Boolean required = false;
	private String mappedWith;
	@ManyToOne
	@JoinColumn(name="category_id")
	private Category category;
	
	@OneToMany(mappedBy = "field")
    private List<SubField> subFields;


}
