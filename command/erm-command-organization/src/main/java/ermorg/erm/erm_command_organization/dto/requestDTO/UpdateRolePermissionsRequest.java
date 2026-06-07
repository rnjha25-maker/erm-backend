package ermorg.erm.erm_command_organization.dto.requestDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UpdateRolePermissionsRequest {
    private List<ResourcePermissionRequest> permissions = new ArrayList<>();
}
