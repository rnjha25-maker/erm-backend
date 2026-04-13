package com.erm.erm_api_gateway.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "custom_view_fields")
@ToString
public class CustomViewFields extends BaseModel {

	private String fieldName;

	@OneToOne
	@JoinColumn(name = "system-field-id")
	private SystemViewField systemViewField;
	@ManyToOne
	@JoinColumn(name = "category_id")
	private ViewCategory category;

}
