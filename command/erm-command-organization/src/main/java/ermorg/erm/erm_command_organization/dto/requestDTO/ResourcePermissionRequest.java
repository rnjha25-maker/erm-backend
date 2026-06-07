package ermorg.erm.erm_command_organization.dto.requestDTO;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.erm_command_organization.model.PermissionAction;
import lombok.Data;

@Data
public class ResourcePermissionRequest {
    private String resourceCode;
    private List<PermissionAction> actions = new ArrayList<>();
}
