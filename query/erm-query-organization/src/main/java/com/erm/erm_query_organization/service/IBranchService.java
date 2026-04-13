package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Branch;

import java.util.List;

public interface IBranchService {
    Branch getBranchById(Long id) throws DataNotFoundException;
    List<Branch> getAllBranches();
}
