package com.erm.dto.riskDTO;

import java.util.Date;

import lombok.Data;

@Data
public class ErmMaturityDto {

	private long maturityId;

    private String assessmentAreaName;

    private Long assessmentArea;

    private String keyAssessmentParameters;

    private String status;

    private String weightageScore;

    private String marksAchieved;

    private String overallMaturityLevel;

    private String assessedBy;

    private Date dueDate;

    private Date actualDate;

    private Date lastAssessmentDate;
    private Date nextAssessmentDate;
}
