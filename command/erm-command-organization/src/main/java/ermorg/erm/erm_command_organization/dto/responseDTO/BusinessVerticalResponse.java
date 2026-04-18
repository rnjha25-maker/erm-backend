package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ermorg.erm.erm_command_organization.model.BusinessVertical;

import lombok.Data;

@Data
public class BusinessVerticalResponse {
    private long organizationId;
    private long companyId;
    private Long branchId;
    private Long departmentId;
    private Long businessSegmentId;
    private List<BusinessVerticalDto> businessVerticals = new ArrayList<>();

    public BusinessVerticalResponse(List<BusinessVertical> businessVerticals, Long organizationId, Long companyId) {
        this(businessVerticals, organizationId, companyId, null, null, null);
    }

    public BusinessVerticalResponse(List<BusinessVertical> businessVerticals, Long organizationId, Long companyId,
            Long branchId, Long departmentId, Long businessSegmentId) {
        this.organizationId = organizationId;
        this.companyId = companyId;
        this.branchId = branchId;
        this.departmentId = departmentId;
        this.businessSegmentId = businessSegmentId;
        this.businessVerticals = businessVerticals.stream()
                .map(bv -> new BusinessVerticalDto(bv))
                .collect(Collectors.toList());
    }
}
