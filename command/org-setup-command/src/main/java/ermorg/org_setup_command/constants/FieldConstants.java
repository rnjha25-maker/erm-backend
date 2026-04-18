package ermorg.org_setup_command.constants;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class FieldConstants {

	private List<String> fields =  new ArrayList<>();
	
	public FieldConstants() {
		fields.add("risk");
		fields.add("category");
		fields.add("likelihood");
		fields.add("impact");
		fields.add("velocity");
		fields.add("rating");
		fields.add("country");
		fields.add("state");
		fields.add("district");
		fields.add("department");
		fields.add("location");
		fields.add("riskLevel");
		fields.add("businessSegmentLevelRisk");
		fields.add("divisionLevelRisk");
		fields.add("process");
		fields.add("mitigationStrategy");
		fields.add("appetite");
		fields.add("tolerance");
		fields.add("annualLossExpectancy");
		
		
		
	}
}
