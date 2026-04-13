package com.erm.erm_query_organization.controller;

import com.erm.erm_query_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_query_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Branch;
import com.erm.erm_query_organization.service.IBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("branch")
public class BranchController {
    @Autowired
    private IBranchService branchService;

    @GetMapping("/all")
    public GeneralResponse<List<Branch>> getAllBranch() {
        GeneralResponse<List<Branch>> response = new GeneralResponse<>();
        List<Branch> branchList = branchService.getAllBranches();
        response.setData(branchList);
        response.setStatus(ResponseStatus.SUCCESS);
        String message = branchList.isEmpty() ? "No branch found!" : "Found " + branchList.size() + " branches";
        response.setMessage(message);
        return response;
    }

    @GetMapping("/{id:[\\d]+}")
    public GeneralResponse<Branch> getBranchById(@PathVariable Long id) {
        GeneralResponse<Branch> response = new GeneralResponse<>();
        try{
            Branch branch = branchService.getBranchById(id);
            response.setData(branch);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Branch found!");
        }catch (DataNotFoundException e){
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
