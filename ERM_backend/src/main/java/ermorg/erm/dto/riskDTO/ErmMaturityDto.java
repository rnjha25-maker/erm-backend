package ermorg.erm.dto.riskDTO;

import java.util.Date;
import java.util.List;

import ermorg.erm.constant.RiskAcceptanceLevel;
import lombok.Data;

@Data
public class ErmMaturityDto {

	private long maturityId;

	private List<Long> departmentIds;

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
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
}
