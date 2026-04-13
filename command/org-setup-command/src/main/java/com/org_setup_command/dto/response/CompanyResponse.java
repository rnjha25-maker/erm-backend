package com.org_setup_command.dto.response;

import java.util.List;

import com.org_setup_command.modal.Company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyResponse {
	
	private long companyId;
	private String companyName;
	private List<BranchResponse> branches;
	
	public CompanyResponse(Company company, List<BranchResponse> branches) {
		
		this.companyId = company.getId();
		this.companyName = company.getName();
		this.branches = branches;
	}

}
