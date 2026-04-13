package com.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import lombok.Data;

@Data
public class BranchDto {

	private long branchId;
	private String branchName;
    private String type;
    private long countryId;
    private long stateId;
    private long cityId;
    private String pincode;
    private String address;
    private String description;
    List<Long> departmentIds;
}
