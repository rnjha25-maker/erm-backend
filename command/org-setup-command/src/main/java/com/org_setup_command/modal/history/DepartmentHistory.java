package com.org_setup_command.modal.history;

import com.org_setup_command.modal.BaseModel;

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
