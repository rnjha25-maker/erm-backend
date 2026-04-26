package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.BusinessSegmentRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessSegmentDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessSegmentResponse;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.BusinessSegment;
import ermorg.erm.erm_command_organization.model.Company;
import ermorg.erm.erm_command_organization.model.Department;
import ermorg.erm.erm_command_organization.repository.BusinessSegmentRepository;
import ermorg.erm.erm_command_organization.repository.CompanyRepository;
import ermorg.erm.erm_command_organization.repository.DepartmentRepository;
import ermorg.erm.erm_command_organization.service.IBusinessSegmentService;
import ermorg.erm.erm_command_organization.util.AuditorAwareImpl;
import ermorg.erm.erm_command_organization.util.OrganizationHierarchyResolver;
import ermorg.erm.erm_command_organization.util.OrganizationHierarchyResolver.Hierarchy;

@Service
public class BusinessSegmentService implements IBusinessSegmentService {

    @Autowired
    private BusinessSegmentRepository businessSegmentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationHierarchyResolver hierarchyResolver;

    @Autowired
    private AuditorAware auditor;

    @Override
    public BusinessSegmentResponse createBusinessSegment(BusinessSegmentRequest request) {
        Hierarchy h = hierarchyResolver.resolve(request.getOrganizationId(), request.getCompanyId(),
                request.getBranchId(), request.getDepartmentId());

        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        List<BusinessSegment> businessSegments = request.getBusinessSegments().stream().map(bs -> {
            BusinessSegment businessSegmentEntity = new BusinessSegment();
            businessSegmentEntity.setName(bs.getSegmentName());
            businessSegmentEntity.setSegmentCode(bs.getSegmentCode());
            businessSegmentEntity.setDescription(bs.getDescription());
            businessSegmentEntity.setOrganization(h.organization());
            businessSegmentEntity.setCompany(h.company());
            businessSegmentEntity.setBranch(h.branch());
            businessSegmentEntity.setDepartment(h.department());
            businessSegmentEntity.setClientIP(clientIp);
            return businessSegmentEntity;
        }).collect(Collectors.toList());

        List<BusinessSegment> savedBusinessSegments = businessSegmentRepository.saveAll(businessSegments);

        return new BusinessSegmentResponse(savedBusinessSegments, h.organization().getId(), h.company().getId(),
                h.branch().getId(), h.department().getId());
    }

    @Override
    public BusinessSegmentResponse updateBusinessSegment(BusinessSegmentRequest request) throws ResourceNotFoundException {
        Hierarchy h = hierarchyResolver.resolve(request.getOrganizationId(), request.getCompanyId(),
                request.getBranchId(), request.getDepartmentId());

        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        List<BusinessSegment> businessSegments = new ArrayList<>();

        for (ermorg.erm.erm_command_organization.dto.requestDTO.BusinessSegment bsRequest : request.getBusinessSegments()) {
            BusinessSegment bs = businessSegmentRepository.findByIdAndDeletedFalse(bsRequest.getBusinessSegmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Business Segment not found."));

            hierarchyResolver.assertBusinessSegmentBelongsToHierarchy(bs, h);

            bs.setName(bsRequest.getSegmentName());
            bs.setSegmentCode(bsRequest.getSegmentCode());
            bs.setDescription(bsRequest.getDescription());
            bs.setClientIP(clientIp);
            businessSegments.add(bs);
        }

        List<BusinessSegment> savedBusinessSegments = businessSegmentRepository.saveAll(businessSegments);

        return new BusinessSegmentResponse(savedBusinessSegments, h.organization().getId(), h.company().getId(),
                h.branch().getId(), h.department().getId());
    }

    @Override
    public void deleteBusinessSegment(Long id) throws InvalidDataException {
        BusinessSegment businessSegment = businessSegmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new InvalidDataException("Business Segment not found."));

        businessSegment.setDeleted(true);
        businessSegment.setClientIP(((AuditorAwareImpl) auditor).getClientIp());
        businessSegmentRepository.save(businessSegment);
    }

    @Override
    public BusinessSegmentDto getBusinessSegment(Long businessSegmentId) throws ResourceNotFoundException {
        BusinessSegment businessSegment = businessSegmentRepository.findByIdAndDeletedFalse(businessSegmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Segment not found."));

        return new BusinessSegmentDto(businessSegment);
    }

    @Override
    public BusinessSegmentResponse getAllBusinessSegmentsByCompanyId(Long companyId) throws ResourceNotFoundException {
        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("No company found"));

        List<BusinessSegment> businessSegments = businessSegmentRepository.findByCompanyIdAndDeletedFalse(companyId);

        return new BusinessSegmentResponse(businessSegments, company.getOrganization().getId(), company.getId());
    }

    @Override
    public BusinessSegmentResponse getAllBusinessSegmentsByDepartmentId(Long departmentId) throws ResourceNotFoundException {
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        if (department.getBranch() == null || department.getBranch().getCompany() == null
                || department.getOrganization() == null) {
            throw new ResourceNotFoundException("Department data is incomplete.");
        }

        List<BusinessSegment> businessSegments = businessSegmentRepository.findByDepartmentIdAndDeletedFalse(departmentId);

        return new BusinessSegmentResponse(businessSegments, department.getOrganization().getId(),
                department.getBranch().getCompany().getId(), department.getBranch().getId(), departmentId);
    }
}
