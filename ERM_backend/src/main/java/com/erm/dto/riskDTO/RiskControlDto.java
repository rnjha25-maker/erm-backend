package com.erm.dto.riskDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class RiskControlDto {

	private long riskControlId;
	private long riskId;
    private List<Long> subRiskIds = new ArrayList<>();
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
    private long primaryResponsible;
    private long approver;
    private Date actualDate;
    private Date lastControlAssessmentDate;
    private Date nextControlAssessmentDate;
}
