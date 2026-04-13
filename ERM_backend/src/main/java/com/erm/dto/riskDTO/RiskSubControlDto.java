package com.erm.dto.riskDTO;

import com.erm.model.RiskSubControl;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RiskSubControlDto {

	private long subControlId;
	private String controlSubTitle;
	
	public RiskSubControlDto(RiskSubControl riskSubControl) {
		this.subControlId = riskSubControl.getId();
		this.controlSubTitle = riskSubControl.getSubControlTitle();
	}
}
