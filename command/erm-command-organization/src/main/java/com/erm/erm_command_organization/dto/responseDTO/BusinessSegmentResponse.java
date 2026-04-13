package com.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.erm.erm_command_organization.model.BusinessSegment;

import lombok.Data;

@Data
public class BusinessSegmentResponse {
    private long organizationId;
    private long companyId;
    private Long branchId;
    private Long departmentId;
    private List<BusinessSegmentDto> businessSegments = new ArrayList<>();

    public BusinessSegmentResponse(List<BusinessSegment> businessSegments, Long organizationId, Long companyId) {
        this(businessSegments, organizationId, companyId, null, null);
    }

    public BusinessSegmentResponse(List<BusinessSegment> businessSegments, Long organizationId, Long companyId,
            Long branchId, Long departmentId) {
        this.organizationId = organizationId;
        this.companyId = companyId;
        this.branchId = branchId;
        this.departmentId = departmentId;
        this.businessSegments = businessSegments.stream()
                .map(bs -> new BusinessSegmentDto(bs))
                .collect(Collectors.toList());
    }
}
