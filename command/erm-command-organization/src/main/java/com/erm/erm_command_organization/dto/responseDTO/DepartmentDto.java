package com.erm.erm_command_organization.dto.responseDTO;

import com.erm.erm_command_organization.model.Department;

import lombok.Data;

@Data
public class DepartmentDto {

	private long departmentId;
	private String departmentName;
	private String desctiption;
	

	
	public DepartmentDto(Department department) {
		this.departmentId = department.getId();
		this.departmentName = department.getName();
		this.desctiption = department.getDescription();
	}
}
