package com.erm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@org.hibernate.annotations.Table(appliesTo = "department",
	indexes = {
		@org.hibernate.annotations.Index(name = "idx_dept_org_deleted", columnNames = {"organization_id", "deleted"})
	}
)
public class Department extends BaseModel{
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
