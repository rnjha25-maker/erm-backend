package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.erm_command_organization.dto.requestDTO.ResourcePermissionRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.UpdateRolePermissionsRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResourceMatrixResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResourcePermissionResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.RolePermissionsResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.UserPermissionsResponse;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.PermissionAction;
import ermorg.erm.erm_command_organization.model.Resource;
import ermorg.erm.erm_command_organization.model.Role;
import ermorg.erm.erm_command_organization.model.RolePermission;
import ermorg.erm.erm_command_organization.repository.ResourceRepository;
import ermorg.erm.erm_command_organization.repository.RolePermissionRepository;
import ermorg.erm.erm_command_organization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final ResourceRepository resourceRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional(readOnly = true)
    public List<ResourceMatrixResponse> getResourceMatrix() {
        return resourceRepository.findByDeletedFalseOrderByDisplayOrderAscNameAsc().stream()
                .filter(resource -> Boolean.TRUE.equals(resource.getActive()))
                .map(this::toMatrixResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPermissionsResponse getUserPermissions(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidDataException("Valid user id is required.");
        }

        List<RolePermission> permissions = rolePermissionRepository.findAllowedByUserId(userId);
        UserPermissionsResponse response = new UserPermissionsResponse();
        response.setUserId(userId);
        response.setResources(groupPermissions(permissions));
        return response;
    }

    @Transactional(readOnly = true)
    public RolePermissionsResponse getRolePermissions(String roleCode) throws ResourceNotFoundException {
        Role role = getRole(roleCode);
        RolePermissionsResponse response = new RolePermissionsResponse();
        response.setRoleCode(role.getRoleCode());
        response.setRoleName(role.getName());
        response.setPermissions(groupPermissions(rolePermissionRepository.findByRoleIdAndDeletedFalse(role.getId())
                .stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getAllowed()))
                .collect(Collectors.toList())));
        return response;
    }

    @Transactional
    public RolePermissionsResponse replaceRolePermissions(String roleCode, UpdateRolePermissionsRequest request)
            throws ResourceNotFoundException {
        Role role = getRole(roleCode);
        validateRequest(request);

        Set<PermissionKey> requested = expandRequest(request);
        List<RolePermission> existingPermissions = rolePermissionRepository.findByRoleIdAndDeletedFalse(role.getId());
        Map<PermissionKey, RolePermission> existing = existingPermissions.stream()
                .collect(Collectors.toMap(this::toKey, Function.identity(), (left, right) -> left));

        for (PermissionKey key : requested) {
            RolePermission current = existing.get(key);
            if (current == null) {
                rolePermissionRepository.save(RolePermission.allowed(role, key.resource(), key.action()));
            } else {
                current.setAllowed(true);
                current.setDeleted(false);
            }
        }

        for (RolePermission current : existingPermissions) {
            if (!requested.contains(toKey(current))) {
                current.setAllowed(false);
            }
        }

        return getRolePermissions(role.getRoleCode());
    }

    private Role getRole(String roleCode) throws ResourceNotFoundException {
        if (roleCode == null || roleCode.isBlank()) {
            throw new InvalidDataException("roleCode is required.");
        }
        return roleRepository.findByRoleCodeAndDeletedFalse(normalizeCode(roleCode))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleCode));
    }

    private void validateRequest(UpdateRolePermissionsRequest request) {
        if (request == null || request.getPermissions() == null) {
            throw new InvalidDataException("permissions are required.");
        }
        for (ResourcePermissionRequest permission : request.getPermissions()) {
            if (permission.getResourceCode() == null || permission.getResourceCode().isBlank()) {
                throw new InvalidDataException("resourceCode is required.");
            }
            if (permission.getActions() == null) {
                throw new InvalidDataException("actions are required.");
            }
        }
    }

    private Set<PermissionKey> expandRequest(UpdateRolePermissionsRequest request) {
        List<String> resourceCodes = request.getPermissions().stream()
                .map(ResourcePermissionRequest::getResourceCode)
                .map(this::normalizeCode)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Resource> resources = resourceRepository.findByResourceCodeInAndDeletedFalse(resourceCodes)
                .stream()
                .collect(Collectors.toMap(Resource::getResourceCode, Function.identity()));

        List<String> missing = resourceCodes.stream()
                .filter(code -> !resources.containsKey(code))
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            throw new InvalidDataException("Invalid resourceCode values: " + missing);
        }

        Set<PermissionKey> requested = new LinkedHashSet<>();
        for (ResourcePermissionRequest item : request.getPermissions()) {
            Resource resource = resources.get(normalizeCode(item.getResourceCode()));
            if (!Boolean.TRUE.equals(resource.getActive())) {
                throw new InvalidDataException("Resource is inactive: " + resource.getResourceCode());
            }
            for (PermissionAction action : item.getActions()) {
                if (action == null) {
                    throw new InvalidDataException("Permission action cannot be null.");
                }
                if (!resource.getSupportedActions().isEmpty() && !resource.getSupportedActions().contains(action)) {
                    throw new InvalidDataException(resource.getResourceCode() + " does not support action " + action);
                }
                requested.add(new PermissionKey(resource, action));
            }
        }
        return requested;
    }

    private List<ResourcePermissionResponse> groupPermissions(List<RolePermission> permissions) {
        Map<String, ResourcePermissionResponse> grouped = new LinkedHashMap<>();

        permissions.stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getAllowed()))
                .filter(permission -> permission.getResource() != null)
                .sorted(Comparator.comparing(permission -> permission.getResource().getName()))
                .forEach(permission -> {
                    Resource resource = permission.getResource();
                    ResourcePermissionResponse item = grouped.computeIfAbsent(resource.getResourceCode(), code -> {
                        ResourcePermissionResponse response = new ResourcePermissionResponse();
                        response.setResourceCode(resource.getResourceCode());
                        response.setResourceName(resource.getName());
                        return response;
                    });
                    if (!item.getActions().contains(permission.getAction())) {
                        item.getActions().add(permission.getAction());
                    }
                });

        grouped.values().forEach(item -> item.getActions().sort(Comparator.comparing(Enum::name)));
        return new ArrayList<>(grouped.values());
    }

    private ResourceMatrixResponse toMatrixResponse(Resource resource) {
        ResourceMatrixResponse response = new ResourceMatrixResponse();
        response.setResourceCode(resource.getResourceCode());
        response.setResourceName(resource.getName());
        response.setResourceType(resource.getResourceType());
        response.setParentResourceCode(resource.getParentResource() == null ? null
                : resource.getParentResource().getResourceCode());
        response.setAvailableActions(resource.getSupportedActions().stream()
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList()));
        return response;
    }

    private PermissionKey toKey(RolePermission permission) {
        return new PermissionKey(permission.getResource(), permission.getAction());
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private record PermissionKey(Resource resource, PermissionAction action) {
    }
}
