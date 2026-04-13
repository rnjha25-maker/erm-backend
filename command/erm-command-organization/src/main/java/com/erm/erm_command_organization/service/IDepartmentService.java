package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.requestDTO.DepartmentRequest;
import com.erm.erm_command_organization.dto.responseDTO.DepartmentDto;
import com.erm.erm_command_organization.dto.responseDTO.DepartmentResponse;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IDepartmentService {
	DepartmentResponse createDepartment(DepartmentRequest request);
	DepartmentResponse updateDepartment(DepartmentRequest request) throws ResourceNotFoundException;
    void deleteDepartment(Long id) throws InvalidDataException;
	DepartmentDto getDepartment(Long departmentId) throws ResourceNotFoundException;
	DepartmentResponse getAllDepartmentByOrganizationId(Long organizationId) throws ResourceNotFoundException;
}
