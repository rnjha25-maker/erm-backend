package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.requestDTO.BranchRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.BranchResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.BranchResponseDto;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IBranchService {
	BranchResponse createBranch(BranchRequest request) throws ResourceNotFoundException;
	BranchResponse updateBranch(BranchRequest request) throws ResourceNotFoundException;
    void deleteBranch(Long id) throws ResourceNotFoundException;
	BranchResponseDto getBranch(Long branchId) throws ResourceNotFoundException;
	BranchResponse getAllBranchesByCompay(Long companyId) throws ResourceNotFoundException;
}
