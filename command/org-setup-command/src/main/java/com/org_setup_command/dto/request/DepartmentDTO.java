package com.org_setup_command.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentDTO {
	
	private long departmentId;
	private String departmentName;
	private long companyId;
	private long branchId;
	private String description;

}
