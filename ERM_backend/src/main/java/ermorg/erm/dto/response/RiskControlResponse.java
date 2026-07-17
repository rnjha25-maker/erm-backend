package ermorg.erm.dto.response;

import java.util.*;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.dto.riskDTO.RiskSubControlDto;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskControl;

import lombok.Data;

@Data
public class RiskControlResponse {

	private long riskControlId;
	private long riskId;
	private String riskTitle;
    private List<SubRiskResponse> riskSubs = new ArrayList<>();
    private String controlSource;
    private String controlTitle;
    private List<RiskSubControlDto> controlSubTitle;
    private String controlType;
    private String controlMechanism;
    private String accessControls;
    private String processControls;
    private String physicalControls;
    private String technicalControl;
    private String controlAssessmentFrequency;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private long primaryResponsible;
    private long approver;
    private Date actualDate;
    private Date lastControlAssessmentDate;
    private Date nextControlAssessmentDate;
    
    public RiskControlResponse(RiskControl riskControl) {
    	this.riskControlId = riskControl.getId();
    	this.riskId = riskControl.getRisk().getId();
		this.riskTitle = riskControl.getRisk().getRisktitle();
		this.controlSource = riskControl.getControlSource();
		this.controlTitle = riskControl.getControlTitle();
		this.controlType = riskControl.getControlType();
		this.controlMechanism = riskControl.getControlMechanism();
		this.accessControls = riskControl.getAccessControls();
		this.processControls = riskControl.getProcessControls();
		this.physicalControls = riskControl.getPhysicalControls();
		this.technicalControl = riskControl.getTechnicalControl();
		this.controlAssessmentFrequency = riskControl.getControlAssessmentFrequency();
		this.riskAppetiteStatus = riskControl.getRiskAppetiteStatus();
		this.riskAcceptanceLevel = riskControl.getRiskAcceptanceLevel();
		this.primaryResponsible = riskControl.getPrimaryResponsible().getId();
		this.approver = riskControl.getApprover().getId();
		this.actualDate = riskControl.getActualDate();
		this.lastControlAssessmentDate = riskControl.getLastControlAssessmentDate();
		this.nextControlAssessmentDate = riskControl.getNextControlAssessmentDate();
		this.riskSubs = Optional.ofNullable(riskControl.getSubRisk())
				.orElse(Collections.emptyList())
				.stream()
				.map(SubRiskResponse::new)
				.toList();

		this.controlSubTitle = Optional.ofNullable(riskControl.getSubControls())
				.orElse(Collections.emptyList())
				.stream()
				.map(RiskSubControlDto::new)
				.toList();
    }
}
