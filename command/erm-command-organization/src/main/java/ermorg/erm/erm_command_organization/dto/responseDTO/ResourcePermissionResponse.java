package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.erm_command_organization.model.PermissionAction;
import lombok.Data;

@Data
public class ResourcePermissionResponse {
    private String resourceCode;
    private String resourceName;
    private List<PermissionAction> actions = new ArrayList<>();
}
