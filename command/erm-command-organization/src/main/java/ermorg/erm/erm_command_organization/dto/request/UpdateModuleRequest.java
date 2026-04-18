package ermorg.erm.erm_command_organization.dto.request;

import java.util.List;

import ermorg.erm.erm_command_organization.dto.requestDTO.OrgCategoryRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.OrgModuleRequest;

import lombok.Data;

@Data
public class UpdateModuleRequest {

	private long orgId;
	List<OrgModuleRequest> orgModules;
}
