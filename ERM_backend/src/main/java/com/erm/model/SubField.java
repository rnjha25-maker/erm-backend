package com.erm.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sub_field")
public class SubField extends BaseModel{

	@Column(name = "sub_field_name")
    private String subFieldName;

    @Column(name = "sub_field_type")
    private String subFieldType;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "mapped_with_column")
    private String mappedWithColumn;

    @ManyToOne
    @JoinColumn(name = "field_id")
    private CustomField field;
}
