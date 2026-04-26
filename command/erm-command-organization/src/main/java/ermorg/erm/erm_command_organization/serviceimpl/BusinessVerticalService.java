package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.BusinessVerticalRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessVerticalDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessVerticalResponse;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.BusinessSegment;
import ermorg.erm.erm_command_organization.model.BusinessVertical;
import ermorg.erm.erm_command_organization.model.Company;
import ermorg.erm.erm_command_organization.model.Department;
import ermorg.erm.erm_command_organization.repository.BusinessSegmentRepository;
import ermorg.erm.erm_command_organization.repository.BusinessVerticalRepository;
import ermorg.erm.erm_command_organization.repository.CompanyRepository;
import ermorg.erm.erm_command_organization.repository.DepartmentRepository;
import ermorg.erm.erm_command_organization.service.IBusinessVerticalService;
import ermorg.erm.erm_command_organization.util.AuditorAwareImpl;
import ermorg.erm.erm_command_organization.util.OrganizationHierarchyResolver;
import ermorg.erm.erm_command_organization.util.OrganizationHierarchyResolver.Hierarchy;

@Service
public class BusinessVerticalService implements IBusinessVerticalService {

    @Autowired
    private BusinessVerticalRepository businessVerticalRepository;

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
    public BusinessVerticalResponse createBusinessVertical(BusinessVerticalRequest request) {
        Hierarchy h = hierarchyResolver.resolve(request.getOrganizationId(), request.getCompanyId(),
                request.getBranchId(), request.getDepartmentId());

        BusinessSegment businessSegment = businessSegmentRepository.findByIdAndDeletedFalse(request.getBusinessSegmentId())
                .orElseThrow(() -> new DataNotFoundException("Business segment not found."));
        hierarchyResolver.assertBusinessSegmentBelongsToHierarchy(businessSegment, h);

        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        List<BusinessVertical> businessVerticals = request.getBusinessVerticals().stream().map(bv -> {
            BusinessVertical businessVerticalEntity = new BusinessVertical();
            businessVerticalEntity.setName(bv.getVerticalName());
            businessVerticalEntity.setVerticalCode(bv.getVerticalCode());
            businessVerticalEntity.setDescription(bv.getDescription());
            businessVerticalEntity.setOrganization(h.organization());
            businessVerticalEntity.setCompany(h.company());
            businessVerticalEntity.setBranch(h.branch());
            businessVerticalEntity.setDepartment(h.department());
            businessVerticalEntity.setBusinessSegment(businessSegment);
            businessVerticalEntity.setClientIP(clientIp);
            return businessVerticalEntity;
        }).collect(Collectors.toList());

        List<BusinessVertical> savedBusinessVerticals = businessVerticalRepository.saveAll(businessVerticals);

        return new BusinessVerticalResponse(savedBusinessVerticals, h.organization().getId(), h.company().getId(),
                h.branch().getId(), h.department().getId(), businessSegment.getId());
    }

    @Override
    public BusinessVerticalResponse updateBusinessVertical(BusinessVerticalRequest request) throws ResourceNotFoundException {
        Hierarchy h = hierarchyResolver.resolve(request.getOrganizationId(), request.getCompanyId(),
                request.getBranchId(), request.getDepartmentId());

        BusinessSegment businessSegment = businessSegmentRepository.findByIdAndDeletedFalse(request.getBusinessSegmentId())
                .orElseThrow(() -> new DataNotFoundException("Business segment not found."));
        hierarchyResolver.assertBusinessSegmentBelongsToHierarchy(businessSegment, h);

        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        List<BusinessVertical> businessVerticals = new ArrayList<>();

        for (ermorg.erm.erm_command_organization.dto.requestDTO.BusinessVertical bvRequest : request.getBusinessVerticals()) {
            BusinessVertical bv = businessVerticalRepository.findByIdAndDeletedFalse(bvRequest.getBusinessVerticalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Business Vertical not found."));

            hierarchyResolver.assertBusinessVerticalBelongsToHierarchy(bv, h, businessSegment);

            bv.setName(bvRequest.getVerticalName());
            bv.setVerticalCode(bvRequest.getVerticalCode());
            bv.setDescription(bvRequest.getDescription());
            bv.setClientIP(clientIp);
            businessVerticals.add(bv);
        }

        List<BusinessVertical> savedBusinessVerticals = businessVerticalRepository.saveAll(businessVerticals);

        return new BusinessVerticalResponse(savedBusinessVerticals, h.organization().getId(), h.company().getId(),
                h.branch().getId(), h.department().getId(), businessSegment.getId());
    }

    @Override
    public void deleteBusinessVertical(Long id) throws InvalidDataException {
        BusinessVertical businessVertical = businessVerticalRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new InvalidDataException("Business Vertical not found."));

        businessVertical.setDeleted(true);
        businessVertical.setClientIP(((AuditorAwareImpl) auditor).getClientIp());
        businessVerticalRepository.save(businessVertical);
    }

    @Override
    public BusinessVerticalDto getBusinessVertical(Long businessVerticalId) throws ResourceNotFoundException {
        BusinessVertical businessVertical = businessVerticalRepository.findByIdAndDeletedFalse(businessVerticalId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Vertical not found."));

        return new BusinessVerticalDto(businessVertical);
    }

    @Override
    public BusinessVerticalResponse getAllBusinessVerticalsByCompanyId(Long companyId) throws ResourceNotFoundException {
        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("No company found"));

        List<BusinessVertical> businessVerticals = businessVerticalRepository.findByCompanyIdAndDeletedFalse(companyId);

        return new BusinessVerticalResponse(businessVerticals, company.getOrganization().getId(), company.getId());
    }

    @Override
    public BusinessVerticalResponse getAllBusinessVerticalsByDepartmentId(Long departmentId) throws ResourceNotFoundException {
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        if (department.getBranch() == null || department.getBranch().getCompany() == null
                || department.getOrganization() == null) {
            throw new ResourceNotFoundException("Department data is incomplete.");
        }

        List<BusinessVertical> businessVerticals = businessVerticalRepository.findByDepartmentIdAndDeletedFalse(departmentId);

        return new BusinessVerticalResponse(businessVerticals, department.getOrganization().getId(),
                department.getBranch().getCompany().getId(), department.getBranch().getId(), departmentId, null);
    }

    @Override
    public BusinessVerticalResponse getAllBusinessVerticalsByBusinessSegmentId(Long businessSegmentId) throws ResourceNotFoundException {
        BusinessSegment segment = businessSegmentRepository.findByIdAndDeletedFalse(businessSegmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Business segment not found."));
        if (segment.getOrganization() == null || segment.getCompany() == null
                || segment.getBranch() == null || segment.getDepartment() == null) {
            throw new ResourceNotFoundException("Business segment hierarchy data is incomplete.");
        }

        List<BusinessVertical> businessVerticals = businessVerticalRepository
                .findByBusinessSegment_IdAndDeletedFalse(businessSegmentId);

        return new BusinessVerticalResponse(businessVerticals, segment.getOrganization().getId(),
                segment.getCompany().getId(), segment.getBranch().getId(), segment.getDepartment().getId(), businessSegmentId);
    }
}
