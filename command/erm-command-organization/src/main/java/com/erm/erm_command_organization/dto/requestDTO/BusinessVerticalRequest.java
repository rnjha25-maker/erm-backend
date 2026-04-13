package com.erm.erm_command_organization.dto.requestDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BusinessVerticalRequest {
    private long organizationId;
    private long companyId;
    private long branchId;
    private long departmentId;
    private long businessSegmentId;
    private List<BusinessVertical> businessVerticals = new ArrayList<>();
}
