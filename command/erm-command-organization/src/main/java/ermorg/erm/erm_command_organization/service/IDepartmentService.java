package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.requestDTO.DepartmentRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.DepartmentDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.DepartmentResponse;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IDepartmentService {
	DepartmentResponse createDepartment(DepartmentRequest request);
	DepartmentResponse updateDepartment(DepartmentRequest request) throws ResourceNotFoundException;
    void deleteDepartment(Long id) throws InvalidDataException;
	DepartmentDto getDepartment(Long departmentId) throws ResourceNotFoundException;
	DepartmentResponse getAllDepartmentByOrganizationId(Long organizationId) throws ResourceNotFoundException;
}
