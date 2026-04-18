package ermorg.org_setup_command.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchDTO {

	long branchId;
	String branchName;
	String branchDescription;
	String type;
	Long countryId;
	Long stateId;
	Long cityId;
	Long companyId;
	String address;
}
