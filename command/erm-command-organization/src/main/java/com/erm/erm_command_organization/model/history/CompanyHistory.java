package com.erm.erm_command_organization.model.history;

import com.erm.erm_command_organization.model.BaseModel;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CompanyHistory extends BaseModel {
    private Long companyId;
    private Long organizationId;
    private String name;
    private String companyLogoImageUrl;
    private String companySite;
    private String pincode;
    private String address;
    private String status;
    private String operation;
    private Long countryId;
    private Long cityId;
    private Long stateId;
    
}
