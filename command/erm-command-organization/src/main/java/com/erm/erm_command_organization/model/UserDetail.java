package com.erm.erm_command_organization.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserDetail extends BaseModel{
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String alternatePhone;
    private String profileImageUrl;
    private String email;
    @ManyToOne//(cascade=CascadeType.MERGE)
    @JoinColumn(name="organization_id")
    private Organization organization;
    
    @ManyToOne//(cascade=CascadeType.MERGE)
    @JoinColumn(name="branch_id")
    private Branch branch;
    @ManyToOne
    private Department department;
}
