package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Department;

import java.util.List;

public interface IDepartmentService {
    Department getDepartmentById(Long id) throws DataNotFoundException;
    List<Department> getAllDepartments();
}
