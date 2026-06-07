package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.erm_command_organization.model.PermissionAction;
import ermorg.erm.erm_command_organization.model.ResourceType;
import lombok.Data;

@Data
public class ResourceMatrixResponse {
    private String resourceCode;
    private String resourceName;
    private ResourceType resourceType;
    private String parentResourceCode;
    private List<PermissionAction> availableActions = new ArrayList<>();
}
