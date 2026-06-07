package ermorg.erm.erm_command_organization.util;

import org.springframework.stereotype.Component;

import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.model.PermissionAction;
import ermorg.erm.erm_command_organization.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermissionChecker {

    private final RolePermissionRepository rolePermissionRepository;

    public boolean hasPermission(Long userId, String resourceCode, PermissionAction action) {
        if (userId == null || resourceCode == null || resourceCode.isBlank() || action == null) {
            return false;
        }
        return rolePermissionRepository.existsAllowedPermission(userId, resourceCode.trim().toUpperCase(), action);
    }

    public void requirePermission(Long userId, String resourceCode, PermissionAction action) {
        if (!hasPermission(userId, resourceCode, action)) {
            throw new InvalidDataException("Missing permission: " + resourceCode + ":" + action);
        }
    }
}
