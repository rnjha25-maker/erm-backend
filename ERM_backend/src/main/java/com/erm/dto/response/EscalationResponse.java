package com.erm.dto.response;

import com.erm.model.Escalation;

import lombok.Data;

@Data
public class EscalationResponse {

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
    
    
    public EscalationResponse(Escalation escalation) {
		this.esclationId = escalation.getId();
		this.priority = escalation.getPriority();
		this.escalationRequired = escalation.getEscalationRequired();
		this.escalationEmailId = escalation.getEscalationEmailId();
		this.emailIdPrimaryResponsible = escalation.getEmailIdPrimaryResponsible();
		this.reportingLevelEmailId = escalation.getReportingLevelEmailId();
		this.escalationResolutionPeriod = escalation.getEscalationResolutionPeriod();
		this.status = escalation.getStatus();
		this.description = escalation.getDescription();
		this.escalationBy = escalation.getEscalationBy();
		this.assignedToPrimaryResponsible = escalation.getAssignedToPrimaryResponsible();
		this.reportingLevel = escalation.getReportingLevel();
		this.action = escalation.getAction();
		this.alertReminder = escalation.getAlertReminder();
		this.remarks = escalation.getRemarks();
	}
}
