package com.erm.dto.riskDTO;

import com.erm.dto.ResponseStatus;
import com.erm.model.Risk;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddRiskResponseDTO {
	private Risk risk;
	private String message;
	private ResponseStatus status;
}
