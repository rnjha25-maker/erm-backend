package com.erm.erm_command_organization.model.history;

import com.erm.erm_command_organization.model.BaseModel;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class DepartmentHistory extends BaseModel {
    private Long departmentId;
    private String name;
    private String description;
    private String operation;
}
