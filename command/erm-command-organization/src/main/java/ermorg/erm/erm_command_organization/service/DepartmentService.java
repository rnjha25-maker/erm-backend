package ermorg.erm.erm_command_organization.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.DepartmentRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.DepartmentDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.DepartmentResponse;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Branch;
import ermorg.erm.erm_command_organization.model.Company;
import ermorg.erm.erm_command_organization.model.Department;
import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.model.history.DepartmentHistory;
import ermorg.erm.erm_command_organization.repository.CompanyRepository;
import ermorg.erm.erm_command_organization.repository.DepartmentRepository;
import ermorg.erm.erm_command_organization.repository.OrganizationRepository;
import ermorg.erm.erm_command_organization.repository.history.DepartmentHistoryRepository;
import ermorg.erm.erm_command_organization.util.AuditorAwareImpl;

@Service
public class DepartmentService implements IDepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AuditorAware auditor;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentHistoryRepository departmentHistoryRepository;
    
    @Autowired
    private CompanyRepository companyResitory;

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) throws DataNotFoundException{
        
    	Organization organization = organizationRepository.findById(request.getOrganizationId()).filter(o -> !o.getDeleted()).orElseThrow(() -> new DataNotFoundException("Invalid Organization ID."));
    	
    	Company company = organization.getCompanies().stream()
    	.filter(c -> !c.getDeleted() && c.getId().equals(request.getCompanyId()))
    	.findAny()
    	.orElseThrow(()-> new DataNotFoundException("No compnay found."));
    	
    	Branch branch = company.getBranches().stream()
    	.filter(b -> !b.getDeleted() && b.getId().equals(request.getBranchId()))
    	.findAny()
    	.orElseThrow(()-> new DataNotFoundException("No branch found."));
    	DepartmentRequest response = new DepartmentRequest();
    	List<Department> departments = request.getDepartments().stream().map(department->{
    		Department departmentEntity = new Department();
    		String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
			departmentEntity.setName(department.getDepartmentName());
			departmentEntity.setClientIP(clientIp);
			departmentEntity.setDescription(department.getDescription());
			departmentEntity.setOrganization(organization);
			departmentEntity.setClientIP(((AuditorAwareImpl) auditor).getClientIp());
			departmentEntity.setBranch(branch);
			return departmentEntity;
			
    	}).collect(Collectors.toList());
    	
    	List<Department> savedDepartments = departmentRepository.saveAll(departments);
    	
    	organization.setDepartments(savedDepartments);
    	
    	organizationRepository.save(organization);
    	
    	response.setOrganizationId(organization.getId());
        
        return new DepartmentResponse(savedDepartments, organization.getId());
}

    @Override
    public DepartmentResponse updateDepartment(DepartmentRequest request) throws ResourceNotFoundException{
    	
    	Organization organization = organizationRepository.findById(request.getOrganizationId()).filter(o -> !o.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Organization not found."));
        
    	List<Department> departments = organization.getDepartments();
    	List<DepartmentHistory> departmentHistoriy = new ArrayList<>();
    	for(Department department : departments) {
    		
    		request.getDepartments().stream().filter(departmentRequest->departmentRequest.getDepartmentId() == department.getId()).findFirst().ifPresent(departmentRequest->{
    			DepartmentHistory departmentHistory = new DepartmentHistory();
	    		departmentHistory.setClientIP(((AuditorAwareImpl) auditor).getClientIp());
	    		departmentHistory.setName(department.getName());
	    		departmentHistory.setDescription(department.getDescription());
	    		departmentHistory.setOperation("U");
    			department.setName(departmentRequest.getDepartmentName());
	    		department.setDescription(departmentRequest.getDescription());
	    		
	    		
	    		
	    		departmentHistoriy.add(departmentHistory);
	    		
    		});
    		
    	}
    	
    	List<Department> savedDepartments = departmentRepository.saveAll(departments);
    	
    	sapveDepartmentHistory(departmentHistoriy);
       
        return new DepartmentResponse(savedDepartments, organization.getId());
    }

    private void sapveDepartmentHistory(List<DepartmentHistory> departmentHistoriy) {
    	
    	departmentHistoryRepository.saveAll(departmentHistoriy);
	}

	@Override
    public void deleteDepartment(Long id) throws InvalidDataException {
       Department department = departmentRepository.findById(id).filter(d -> !d.getDeleted()).orElseThrow(() -> new InvalidDataException("Department not found."));
		
       department.setDeleted(true);
        
       departmentRepository.save(department);
       
       DepartmentHistory departmentHistory = new DepartmentHistory();
		departmentHistory.setClientIP(((AuditorAwareImpl) auditor).getClientIp());
		departmentHistory.setName(department.getName());
		departmentHistory.setDescription(department.getDescription());
		departmentHistory.setOperation("D");
		
		sapveDepartmentHistory(List.of(departmentHistory));
    }

	@Override
	public DepartmentDto getDepartment(Long departmentId) throws ResourceNotFoundException {
		
		Department department = departmentRepository.findById(departmentId).filter(d -> !d.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Department not found."));
		
		return new DepartmentDto(department);
	}

	@Override
	public DepartmentResponse getAllDepartmentByOrganizationId(Long departmentId) throws ResourceNotFoundException {
		
		Organization organization = organizationRepository.findById(departmentId).filter(d -> !d.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Organization not found."));
		
		
		return new DepartmentResponse(organization.getDepartments(), organization.getId());
	}
	
	
}
