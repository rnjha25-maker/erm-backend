package ermorg.erm.erm_command_organization.dto.requestDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BranchRequest {

	private long organizationId;
	private long companyId;
	private List<BranchDto> branches = new ArrayList<>();
}
