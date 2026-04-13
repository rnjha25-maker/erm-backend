package com.erm.model.translation;

import com.erm.model.BaseModel;
import com.erm.model.SubRisk;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SubRiskTranslation extends BaseModel{

	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="sub_risk_id")
	private SubRisk subRisk;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="language_id")
	private Language language;
}
