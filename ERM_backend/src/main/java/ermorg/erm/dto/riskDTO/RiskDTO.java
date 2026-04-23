package ermorg.erm.dto.riskDTO;

import java.util.List;
import ermorg.erm.constant.RiskCategory;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RiskDTO {
	private Long riskId;
	private String risktitle;
	private String riskSource;
    private RiskCategory category;
    private String subCategory;
    private String exposure;
    private long function;
    private long businessVertical;
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
