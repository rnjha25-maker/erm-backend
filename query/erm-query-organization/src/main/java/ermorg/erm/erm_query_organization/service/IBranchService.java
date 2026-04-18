package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.Branch;

import java.util.List;

public interface IBranchService {
    Branch getBranchById(Long id) throws DataNotFoundException;
    List<Branch> getAllBranches();
}
