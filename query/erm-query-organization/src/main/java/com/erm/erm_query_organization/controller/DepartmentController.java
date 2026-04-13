package com.erm.erm_query_organization.controller;

import com.erm.erm_query_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_query_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Department;
import com.erm.erm_query_organization.service.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("department")
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

    @GetMapping("/all")
    public GeneralResponse<List<Department>> getAllDepartments() {
        GeneralResponse<List<Department>> response = new GeneralResponse<>();
        List<Department> departmentList = departmentService.getAllDepartments();
        response.setData(departmentList);
        response.setStatus(ResponseStatus.SUCCESS);
        String message = departmentList.isEmpty() ? "No department found!" : "Found " + departmentList.size() + " departments";
        return response;
    }

    @GetMapping("/{id:[\\d]+}")
    public GeneralResponse<Department> getDepartmentById(@PathVariable Long id) {
        GeneralResponse<Department> response = new GeneralResponse<>();
        try{
            Department department = departmentService.getDepartmentById(id);
            response.setData(department);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Department found!");
        }catch (DataNotFoundException e){
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
