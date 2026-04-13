package com.org_setup_command.modal.history;

import com.org_setup_command.modal.BaseModel;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CompanyHistory extends BaseModel {
    private Long companyId;
    private String name;
    private String companyLogoImageUrl;
    private String operation;
    private Long organizationId;
}
