package com.erm.dto.riskDTO;

import java.util.List;

import com.erm.constant.BusinessVertical;
import com.erm.constant.Functions;
import com.erm.constant.RiskCategory;
import com.erm.constant.RiskSubCategory;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RiskDTO {
	private Long riskId;
	private String risktitle;
	private String riskSource;
    private RiskCategory category;
    private RiskSubCategory subCategory;
    private String exposure;
    private Functions function;
    private BusinessVertical businessVertical;
    private String businessSegment;
    private long riskOwnerId;
    private long riskChampionId;
    private String riskCreationByPeriod;
    private String riskStatus;
    private String evidanceRequired;
    private String riskRegisterType; 
    private String supportingEvidance; 
    private long branchId;
    private List<SubRiskDTO> subRisk;
    
}
