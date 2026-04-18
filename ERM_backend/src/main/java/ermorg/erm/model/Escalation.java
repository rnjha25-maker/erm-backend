package ermorg.erm.model;

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
@Table(name = "escalations")
public class Escalation extends BaseModel {

	@Column(name = "priority")
    private String priority;
    
    @Column(name = "escalation_required")
    private String escalationRequired;
    
    @Column(name = "escalation_email_id")
    private String escalationEmailId;
    
    @Column(name = "email_id_primary_responsible")
    private String emailIdPrimaryResponsible;
    
    @Column(name = "reporting_level_email_id")
    private String reportingLevelEmailId;
    
    @Column(name = "escalation_resolution_period")
    private String escalationResolutionPeriod;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "escalation_by")
    private String escalationBy;
    
    @Column(name = "assigned_to_primary_responsible")
    private String assignedToPrimaryResponsible;
    
    @Column(name = "reporting_level")
    private String reportingLevel;
    
    @Column(name = "action", columnDefinition = "TEXT")
    private String action;
    
    @Column(name = "alert_reminder")
    private String alertReminder;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @ManyToOne
	@JoinColumn(name = "organization_id")
    private Organization organization;
    
	@ManyToOne
	@JoinColumn(name = "company_id")
    private Company company;
}
