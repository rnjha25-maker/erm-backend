package com.org_setup_command.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org_setup_command.dto.request.DepartmentDTO;
import com.org_setup_command.dto.response.DepartmentResponse;
import com.org_setup_command.exception.InvalidResourceAccess;
import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.Branch;
import com.org_setup_command.modal.Company;
import com.org_setup_command.modal.Department;
import com.org_setup_command.modal.Organization;
import com.org_setup_command.repository.CompanyRepository;
import com.org_setup_command.repository.DepartmentRepository;
import com.org_setup_command.util.OrganizationContext;

import jakarta.transaction.Transactional;

@Service
public class DepartmentService implements IDepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Override
	public DepartmentResponse createDepartment(DepartmentDTO request)
			throws ResourceNotFoundException, InvalidResourceAccess {

		
		Company company = companyRepository.findById(request.getCompanyId()).filter(cmp -> !cmp.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No company found."));

		Branch branch = company.getBranches().stream().filter(br -> br.getId().equals(request.getBranchId())).findAny()
				.orElseThrow(() -> new ResourceNotFoundException("No branch found."));

		Department department = new Department();
		department.setName(request.getDepartmentName());
		department.setDescription(request.getDescription());
		department.setBranch(branch);

		Department savedDepartment = departmentRepository.save(department);

		return new DepartmentResponse(savedDepartment);
	}

	@Override
	public DepartmentResponse updateDepartment(DepartmentDTO request)
			throws ResourceNotFoundException, InvalidResourceAccess {

		Department department = departmentRepository.findById(request.getDepartmentId())
				.filter(d -> !d.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Department not found."));
			
		department.setName(request.getDepartmentName());
		department.setDescription(request.getDescription());

		Department savedDepartment = departmentRepository.save(department);

		return new DepartmentResponse(savedDepartment);
	}

	@Transactional
	@Override
	public void deleteDepartment(Long departmentId) throws InvalidDataException, ResourceNotFoundException, InvalidResourceAccess {
	
		Department department = departmentRepository.findById(departmentId)
				.filter(d-> !d.getDeleted())
				.orElseThrow(()-> new ResourceNotFoundException("No department found."));
		
		department.setDeleted(true);
		
		departmentRepository.save(department);
		
		
		
		
	}

	@Override
	public List<DepartmentResponse> getAllDepartments(Long branchId) throws ResourceNotFoundException, InvalidResourceAccess {
		
		List<Department> departments = departmentRepository.getDepartmentsByBranchId(branchId);
		if(departments == null) throw new ResourceNotFoundException("No department found");
		
		return departments.stream()
		.map(d -> new DepartmentResponse(d))
		.collect(Collectors.toList());
		
	}

	@Override
	public DepartmentResponse getDepartment(Long departmentId) throws ResourceNotFoundException, InvalidResourceAccess {
		
		Department department = departmentRepository.findById(departmentId)
		.filter(d -> !d.getDeleted())
		.orElseThrow(()-> new ResourceNotFoundException("No department found."));
		
		
		return new DepartmentResponse(department);
	}
	
	
}
