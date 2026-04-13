package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Department;
import com.erm.erm_query_organization.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService implements IDepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Department getDepartmentById(Long id) throws DataNotFoundException{
        Optional<Department> departmentOptional = departmentRepository.findById(id);
        if(departmentOptional.isEmpty()){
            throw new DataNotFoundException("Department not found");
        }
        return departmentOptional.get();
    }

    @Override
    public List<Department> getAllDepartments(){
        return departmentRepository.findAll();
    }

}
