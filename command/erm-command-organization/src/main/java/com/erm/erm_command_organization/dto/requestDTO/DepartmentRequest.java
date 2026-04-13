package com.erm.erm_command_organization.dto.requestDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DepartmentRequest {
	
	private long organizationId;
	private long companyId;
	private long branchId;
	private List<Department> departments = new ArrayList<>();
	

}
