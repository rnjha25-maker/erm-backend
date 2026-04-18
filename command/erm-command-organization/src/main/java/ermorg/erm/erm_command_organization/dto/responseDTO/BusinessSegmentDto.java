package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.BusinessSegment;

import lombok.Data;

@Data
public class BusinessSegmentDto {
    private long businessSegmentId;
    private String segmentName;
    private String businessSegmentName;
    private String segmentCode;
    private String description;
    private Long organizationId;
    private Long companyId;
    private Long branchId;
    private Long departmentId;

    public BusinessSegmentDto(BusinessSegment businessSegment) {
        this.businessSegmentId = businessSegment.getId();
        this.segmentName = businessSegment.getName();
        this.businessSegmentName = businessSegment.getName();
        this.segmentCode = businessSegment.getSegmentCode();
        this.description = businessSegment.getDescription();
        this.organizationId = businessSegment.getOrganization() != null ? businessSegment.getOrganization().getId() : null;
        this.companyId = businessSegment.getCompany() != null ? businessSegment.getCompany().getId() : null;
        this.branchId = businessSegment.getBranch() != null ? businessSegment.getBranch().getId() : null;
        this.departmentId = businessSegment.getDepartment() != null ? businessSegment.getDepartment().getId() : null;
    }
}
