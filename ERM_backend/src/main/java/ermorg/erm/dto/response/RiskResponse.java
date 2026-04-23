package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;
import ermorg.erm.constant.RiskCategory;
import ermorg.erm.model.Risk;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RiskResponse {
	
	private Long riskId;
	private String risktitle;
	private String riskSource;
    private RiskCategory category;
    private String subCategory;
    private String exposure;
    private Long function;
    private Long businessVertical;
    private String businessSegment;
    private long riskOwnerId;
    private long riskChampionId;
    private String riskCreationByPeriod;
    private String riskStatus;
    private String evidanceRequired;
    private String riskRegisterType; 
    private String supportingEvidance; 
    private long branchId;
    
    private List<SubRiskResponse> subRisk = new ArrayList<>();
    
    public RiskResponse(Risk risk) {
    	this.riskId = risk.getId();
		this.risktitle = risk.getRisktitle();
		this.riskSource = risk.getRiskSource();
		this.category = risk.getCategory();
		this.subCategory = risk.getSubCategory();
		this.exposure = risk.getExposure();
		this.function = risk.getFunction();
		this.businessVertical = risk.getBusinessVertical();
		this.businessSegment = risk.getBusinessSegment();
		this.riskOwnerId = risk.getRiskOwner().getId();
		this.riskChampionId = risk.getRiskChampion().getId();
		this.riskCreationByPeriod = risk.getRiskCreationByPeriod();
		this.riskStatus = risk.getRiskStatus();
		this.evidanceRequired = risk.getEvidanceRequired();
		this.riskRegisterType = risk.getRiskRegisterType();
		this.supportingEvidance = risk.getSupportingEvidance();
		this.branchId = risk.getBranchId();
		
		this.subRisk = risk.getSubRisk().stream().map(SubRiskResponse::new).toList();
		
    }

}
