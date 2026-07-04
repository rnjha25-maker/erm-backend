package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;
import ermorg.erm.constant.RiskCategory;
import ermorg.erm.model.Risk;
import ermorg.erm.model.User;
import ermorg.erm.model.UserDetail;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RiskResponse {

	private Long riskId;
	private String risktitle;
	private String riskTitle;
	private String riskSource;
	private RiskCategory category;
	private String subCategory;
	private String exposure;
	private Long function;
	private String functionName;
	private Long businessVertical;
	private String businessVerticalName;
	private String businessSegment;
	private String businessSegmentName;
	private long riskOwnerId;
	private String riskOwnerName;
	private long riskChampionId;
	private String riskChampionName;
	private String riskCreationByPeriod;
	private String riskStatus;
	private String evidanceRequired;
	private String riskRegisterType;
	private String supportingEvidance;
	private Long branchId;
	private String branchName;

	private List<RiskAssessmentResponse> riskAssessments = new ArrayList<>();

	private List<SubRiskResponse> subRisk = new ArrayList<>();

	public RiskResponse(Risk risk) {
		this.riskId = risk.getId();
		this.risktitle = risk.getRisktitle();
		this.riskTitle = risk.getRisktitle();
		this.riskSource = risk.getRiskSource();
		this.category = risk.getCategory();
		this.subCategory = risk.getSubCategory();
		this.exposure = risk.getExposure();
		this.function = risk.getFunction();
		this.businessVertical = risk.getBusinessVertical();
		this.businessSegment = risk.getBusinessSegment();
		this.businessSegmentName = risk.getBusinessSegment();
		this.riskOwnerId = risk.getRiskOwner() != null ? risk.getRiskOwner().getId() : 0;
		this.riskOwnerName = formatUserName(risk.getRiskOwner());
		this.riskChampionId = risk.getRiskChampion() != null ? risk.getRiskChampion().getId() : 0;
		this.riskChampionName = formatUserName(risk.getRiskChampion());
		this.riskCreationByPeriod = risk.getRiskCreationByPeriod();
		this.riskStatus = risk.getRiskStatus();
		this.evidanceRequired = risk.getEvidanceRequired();
		this.riskRegisterType = risk.getRiskRegisterType();
		this.supportingEvidance = risk.getSupportingEvidance();
		this.branchId = risk.getBranchId();
		if (risk.getRiskAssessments() != null) {
			this.riskAssessments = risk.getRiskAssessments().stream()
					.filter(a -> !a.getDeleted())
					.map(RiskAssessmentResponse::new)
					.toList();
		}

		this.subRisk = risk.getSubRisk() != null ? risk.getSubRisk().stream().map(SubRiskResponse::new).toList()
				: new ArrayList<>();

	}

	private String formatUserName(User user) {
		if (user == null) {
			return null;
		}

		UserDetail detail = user.getUserDetail();
		if (detail == null) {
			return user.getEmail();
		}

		String name = String.join(" ",
				nullToBlank(detail.getFirstName()),
				nullToBlank(detail.getMiddleName()),
				nullToBlank(detail.getLastName())).trim().replaceAll("\\s+", " ");

		return name.isBlank() ? user.getEmail() : name;
	}

	private String nullToBlank(String value) {
		return value != null ? value : "";
	}

}
