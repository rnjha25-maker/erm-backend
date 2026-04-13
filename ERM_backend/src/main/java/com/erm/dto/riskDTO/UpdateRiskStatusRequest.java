package com.erm.dto.riskDTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateRiskStatusRequest {
	private Long riskId;
	private String status;
}
