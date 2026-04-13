package com.erm.dto.riskDTO;

import lombok.Data;

@Data
public class EscalationRequestDto {

	private long esclationId;
	private String priority;
    private String escalationRequired;
    private String escalationEmailId;
    private String emailIdPrimaryResponsible;
    private String reportingLevelEmailId;
    private String escalationResolutionPeriod;
    private String status;
    private String description;
    private String escalationBy;
    private String assignedToPrimaryResponsible;
    private String reportingLevel;
    private String action;
    private String alertReminder;
    private String remarks;
}
