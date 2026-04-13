package com.erm.erm_command_organization.model.history;

import com.erm.erm_command_organization.model.BaseModel;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BranchHistory extends BaseModel {
	private Long organizationId;
	private Long companyId;
    private Long branchId;
    private String name;
    private String description;
    private String type;
    private Long countryId;
    private Long stateId;
    private Long cityId;
    private String address;
	private String pincode;
	private String status;
    private String operation;
}
