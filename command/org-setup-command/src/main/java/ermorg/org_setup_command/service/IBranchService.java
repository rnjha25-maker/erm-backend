package ermorg.org_setup_command.service;

import java.util.List;

import ermorg.org_setup_command.dto.request.BranchDTO;
import ermorg.org_setup_command.dto.response.BranchResponse;
import ermorg.org_setup_command.exception.ResourceNotFoundException;

public interface IBranchService {
    public BranchResponse createBranch(BranchDTO request) throws ResourceNotFoundException;
    public BranchResponse updateBranch(BranchDTO request) throws ResourceNotFoundException, InvalidDataException;
    public BranchResponse deleteBranch(Long id) throws InvalidDataException, ResourceNotFoundException;
    public BranchResponse getBranch(Long id)throws ResourceNotFoundException;
    public List<BranchResponse> getAllBranch(Long companyId) throws ResourceNotFoundException;
}
