package ermorg.erm.erm_command_organization.util;

import org.springframework.stereotype.Component;

import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.model.Branch;
import ermorg.erm.erm_command_organization.model.BusinessSegment;
import ermorg.erm.erm_command_organization.model.BusinessVertical;
import ermorg.erm.erm_command_organization.model.Company;
import ermorg.erm.erm_command_organization.model.Department;
import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.repository.BranchRepository;
import ermorg.erm.erm_command_organization.repository.CompanyRepository;
import ermorg.erm.erm_command_organization.repository.DepartmentRepository;
import ermorg.erm.erm_command_organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

/**
 * Validates organization hierarchy (organization → company → branch → department) for command APIs.
 * Use {@link #requireOrganization(Long)} when creating a company; {@link #requireCompany(Long, Long)}
 * when creating a branch; {@link #requireBranch(Long, Long, Long)} when creating a department;
 * {@link #resolve(Long, Long, Long, Long)} or {@link #requireDepartment(Long, Long, Long, Long)} when
 * the full chain is required (e.g. business segment / vertical).
 */
@Component
@RequiredArgsConstructor
public class OrganizationHierarchyResolver {

    private final OrganizationRepository organizationRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;

    public record Hierarchy(Organization organization, Company company, Branch branch, Department department) {}

    /**
     * Validates the full hierarchy and returns all resolved entities (e.g. business segment / vertical flows).
     */
    public Hierarchy resolve(Long organizationId, Long companyId, Long branchId, Long departmentId) {
        Organization organization = requireOrganization(organizationId);
        Company company = requireCompany(organization, companyId);
        Branch branch = requireBranch(organization, company, branchId);
        Department department = requireDepartment(organization, branch, departmentId);
        return new Hierarchy(organization, company, branch, department);
    }

    /** Use when an API only needs to assert the organization exists (e.g. creating a company under it). */
    public Organization requireOrganization(Long organizationId) {
        return organizationRepository.findByIdAndDeletedFalse(organizationId)
                .orElseThrow(() -> new DataNotFoundException("Organization not found."));
    }

    /** Use when an API needs organization + company (e.g. creating a branch). */
    public Company requireCompany(Long organizationId, Long companyId) {
        Organization organization = requireOrganization(organizationId);
        return requireCompany(organization, companyId);
    }

    /** Use when the {@link Organization} is already loaded to avoid an extra fetch. */
    public Company requireCompany(Organization organization, Long companyId) {
        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new DataNotFoundException("Company not found."));
        ensureCompanyBelongsToOrganization(company, organization.getId());
        return company;
    }

    /** Use when an API needs organization + company + branch (e.g. creating departments). */
    public Branch requireBranch(Long organizationId, Long companyId, Long branchId) {
        Organization organization = requireOrganization(organizationId);
        Company company = requireCompany(organization, companyId);
        return requireBranch(organization, company, branchId);
    }

    /** Use when {@link Organization} and {@link Company} are already loaded. */
    public Branch requireBranch(Organization organization, Company company, Long branchId) {
        Branch branch = branchRepository.findByIdAndDeletedFalse(branchId)
                .orElseThrow(() -> new DataNotFoundException("Branch not found."));
        ensureBranchBelongsToCompanyAndOrganization(branch, organization.getId(), company.getId());
        return branch;
    }

    /** Use when the full chain down to department is required. */
    public Department requireDepartment(Long organizationId, Long companyId, Long branchId, Long departmentId) {
        Organization organization = requireOrganization(organizationId);
        Company company = requireCompany(organization, companyId);
        Branch branch = requireBranch(organization, company, branchId);
        return requireDepartment(organization, branch, departmentId);
    }

    /** Use when {@link Organization} and {@link Branch} are already loaded. */
    public Department requireDepartment(Organization organization, Branch branch, Long departmentId) {
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> new DataNotFoundException("Department not found."));
        ensureDepartmentBelongsToBranchAndOrganization(department, organization.getId(), branch.getId());
        return department;
    }

    private static void ensureCompanyBelongsToOrganization(Company company, Long organizationId) {
        if (company.getOrganization() == null || !company.getOrganization().getId().equals(organizationId)) {
            throw new DataNotFoundException("Company does not belong to the given organization.");
        }
    }

    private static void ensureBranchBelongsToCompanyAndOrganization(Branch branch, Long organizationId, Long companyId) {
        if (branch.getCompany() == null || !branch.getCompany().getId().equals(companyId)
                || branch.getOrganization() == null || !branch.getOrganization().getId().equals(organizationId)) {
            throw new DataNotFoundException("Branch does not belong to the given company or organization.");
        }
    }

    private static void ensureDepartmentBelongsToBranchAndOrganization(Department department, Long organizationId,
            Long branchId) {
        if (department.getBranch() == null || !department.getBranch().getId().equals(branchId)
                || department.getOrganization() == null || !department.getOrganization().getId().equals(organizationId)) {
            throw new DataNotFoundException("Department does not belong to the given branch or organization.");
        }
    }

    public void assertBusinessSegmentBelongsToHierarchy(BusinessSegment segment, Hierarchy h) {
        if (segment.getOrganization() == null || !segment.getOrganization().getId().equals(h.organization().getId())) {
            throw new DataNotFoundException("Business segment does not belong to the given organization.");
        }
        if (segment.getCompany() == null || !segment.getCompany().getId().equals(h.company().getId())) {
            throw new DataNotFoundException("Business segment does not belong to the given company.");
        }
        if (segment.getBranch() == null || !segment.getBranch().getId().equals(h.branch().getId())) {
            throw new DataNotFoundException("Business segment does not belong to the given branch.");
        }
        if (segment.getDepartment() == null || !segment.getDepartment().getId().equals(h.department().getId())) {
            throw new DataNotFoundException("Business segment does not belong to the given department.");
        }
    }

    public void assertBusinessVerticalBelongsToHierarchy(BusinessVertical vertical, Hierarchy h, BusinessSegment expectedSegment) {
        if (vertical.getOrganization() == null || !vertical.getOrganization().getId().equals(h.organization().getId())) {
            throw new DataNotFoundException("Business vertical does not belong to the given organization.");
        }
        if (vertical.getCompany() == null || !vertical.getCompany().getId().equals(h.company().getId())) {
            throw new DataNotFoundException("Business vertical does not belong to the given company.");
        }
        if (vertical.getBranch() == null || !vertical.getBranch().getId().equals(h.branch().getId())) {
            throw new DataNotFoundException("Business vertical does not belong to the given branch.");
        }
        if (vertical.getDepartment() == null || !vertical.getDepartment().getId().equals(h.department().getId())) {
            throw new DataNotFoundException("Business vertical does not belong to the given department.");
        }
        if (vertical.getBusinessSegment() == null || !vertical.getBusinessSegment().getId().equals(expectedSegment.getId())) {
            throw new DataNotFoundException("Business vertical does not belong to the given business segment.");
        }
    }
}
