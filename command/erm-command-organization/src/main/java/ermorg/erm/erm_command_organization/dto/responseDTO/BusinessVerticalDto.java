package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.BusinessVertical;

import lombok.Data;

@Data
public class BusinessVerticalDto {
    private long businessVerticalId;
    private String verticalName;
    private String businessVerticalName;
    private String verticalCode;
    private String description;
    private Long organizationId;
    private Long companyId;
    private Long branchId;
    private Long departmentId;
    private Long businessSegmentId;

    public BusinessVerticalDto(BusinessVertical businessVertical) {
        this.businessVerticalId = businessVertical.getId();
        this.verticalName = businessVertical.getName();
        this.businessVerticalName = businessVertical.getName();
        this.verticalCode = businessVertical.getVerticalCode();
        this.description = businessVertical.getDescription();
        this.organizationId = businessVertical.getOrganization() != null ? businessVertical.getOrganization().getId() : null;
        this.companyId = businessVertical.getCompany() != null ? businessVertical.getCompany().getId() : null;
        this.branchId = businessVertical.getBranch() != null ? businessVertical.getBranch().getId() : null;
        this.departmentId = businessVertical.getDepartment() != null ? businessVertical.getDepartment().getId() : null;
        this.businessSegmentId = businessVertical.getBusinessSegment() != null ? businessVertical.getBusinessSegment().getId() : null;
    }
}
