package com.erm.dto.response;

import com.erm.model.Risk;

import lombok.Data;

@Data
public class AllRiskDropdownResponse {

	private Long riskId;
	private String riskName;
	
	public AllRiskDropdownResponse(Risk risk) {
		this.riskId = risk.getId();
		this.riskName = risk.getRisktitle();
	}
	
}
