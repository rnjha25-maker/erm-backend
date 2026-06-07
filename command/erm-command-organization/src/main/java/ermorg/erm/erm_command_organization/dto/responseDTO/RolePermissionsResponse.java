package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RolePermissionsResponse {
    private String roleCode;
    private String roleName;
    private List<ResourcePermissionResponse> permissions = new ArrayList<>();
}
