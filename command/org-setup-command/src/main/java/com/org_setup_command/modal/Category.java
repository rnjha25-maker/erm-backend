package com.org_setup_command.modal;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "category")
public class Category extends BaseModel{
    
    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "mapped_with_table")
    private String mappedWithTable;

    @OneToMany(mappedBy = "category")
    private List<CustomField> fields;

    @ManyToOne
    @JoinColumn(name="module_id")
    private Modules module;

	public Category(String categoryName, String mappedWithTable ) {
		super();
		this.categoryName = categoryName;
		this.mappedWithTable = mappedWithTable;
	}
}
