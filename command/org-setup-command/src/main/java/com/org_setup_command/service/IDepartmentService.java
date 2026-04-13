package com.org_setup_command.service;

import java.util.List;

import com.org_setup_command.dto.request.DepartmentDTO;
import com.org_setup_command.dto.response.DepartmentResponse;
import com.org_setup_command.exception.InvalidResourceAccess;
import com.org_setup_command.exception.ResourceNotFoundException;

public interface IDepartmentService {
	public DepartmentResponse createDepartment(DepartmentDTO request) throws ResourceNotFoundException, InvalidResourceAccess;
	public DepartmentResponse updateDepartment(DepartmentDTO request) throws ResourceNotFoundException, InvalidResourceAccess;
	public List<DepartmentResponse> getAllDepartments(Long branchId)  throws ResourceNotFoundException, InvalidResourceAccess;
    public void deleteDepartment(Long departmentId) throws InvalidDataException, ResourceNotFoundException, InvalidResourceAccess;
	public DepartmentResponse getDepartment(Long departmentId)throws ResourceNotFoundException, InvalidResourceAccess;
}
