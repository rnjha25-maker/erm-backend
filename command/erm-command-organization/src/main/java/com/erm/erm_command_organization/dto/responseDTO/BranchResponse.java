package com.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.erm.erm_command_organization.model.Branch;

import lombok.Data;

@Data
public class BranchResponse {

	private long organizationId;
	private long companyId;
	
	private List<BranchResponseDto> branches = new ArrayList<>();
	
	public BranchResponse(List<Branch> branches, long organizationId, long companyId) {
		this.organizationId = organizationId;
		this.companyId = companyId;
		this.branches = branches.stream().map(branch->new BranchResponseDto(branch)).collect(Collectors.toList());
	}
}
