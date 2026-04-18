package ermorg.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import lombok.Data;

@Data
public class OrgModuleRequest {
	private long moduleId;
	private List<OrgCategoryRequest> categories;

}
