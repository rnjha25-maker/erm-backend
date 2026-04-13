package com.erm.dto.response;

import com.erm.model.SubRisk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubRiskResponse {
	
	private long subRiskId;
	private String subRiskName;
	
	public SubRiskResponse(SubRisk subRisk) {
		this.subRiskId = subRisk.getId();
		this.subRiskName = subRisk.getSubRisk();
	}

	@Override
	public String toString() {
		return  subRiskId + "";
	}

	
	
}
