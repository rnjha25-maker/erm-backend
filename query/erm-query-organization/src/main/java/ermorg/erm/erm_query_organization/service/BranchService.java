package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.Branch;
import ermorg.erm.erm_query_organization.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchService implements IBranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public Branch getBranchById(Long id) throws DataNotFoundException {
        Optional<Branch> branchOptional = branchRepository.findById(id);
        if(branchOptional.isEmpty()){
            throw new DataNotFoundException("Branch not found");
        }
        return branchOptional.get();
    }

    @Override
    public List<Branch> getAllBranches(){
        return branchRepository.findAll();
    }

}
