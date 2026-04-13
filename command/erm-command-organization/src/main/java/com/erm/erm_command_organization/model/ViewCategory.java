package com.erm.erm_command_organization.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "view_category")
public class ViewCategory extends BaseModel {


    
    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "mapped_with_table")
    private String mappedWithTable;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<CustomViewFields> fields = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="module_id")
    private Modules module;


}
