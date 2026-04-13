package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.requestDTO.BranchRequest;
import com.erm.erm_command_organization.dto.responseDTO.BranchResponse;
import com.erm.erm_command_organization.dto.responseDTO.BranchResponseDto;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IBranchService {
	BranchResponse createBranch(BranchRequest request) throws ResourceNotFoundException;
	BranchResponse updateBranch(BranchRequest request) throws ResourceNotFoundException;
    void deleteBranch(Long id) throws ResourceNotFoundException;
	BranchResponseDto getBranch(Long branchId) throws ResourceNotFoundException;
	BranchResponse getAllBranchesByCompay(Long companyId) throws ResourceNotFoundException;
}
