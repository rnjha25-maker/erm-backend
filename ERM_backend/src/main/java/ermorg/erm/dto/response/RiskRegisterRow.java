package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RiskRegisterRow {

	private List<CustomResponse> risk = new ArrayList<>();
	private List<CustomResponse> riskAssessment = new ArrayList<>();
	private List<CustomResponse> riskControl = new ArrayList<>();
	private List<CustomResponse> riskResponse = new ArrayList<>();
	private List<CustomResponse> riskReview = new ArrayList<>();
}
