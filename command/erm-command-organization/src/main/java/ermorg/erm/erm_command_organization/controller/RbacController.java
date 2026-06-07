package ermorg.erm.erm_command_organization.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.requestDTO.UpdateRolePermissionsRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResourceMatrixResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.dto.responseDTO.RolePermissionsResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.serviceimpl.RbacService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService rbacService;

    @GetMapping("/resources")
    public GeneralResponse<List<ResourceMatrixResponse>> getResources() {
        GeneralResponse<List<ResourceMatrixResponse>> response = new GeneralResponse<>();
        response.setData(rbacService.getResourceMatrix());
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }

    @GetMapping("/roles/{roleCode}/permissions")
    public GeneralResponse<RolePermissionsResponse> getRolePermissions(@PathVariable String roleCode)
            throws ResourceNotFoundException {
        GeneralResponse<RolePermissionsResponse> response = new GeneralResponse<>();
        response.setData(rbacService.getRolePermissions(roleCode));
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }

    @PutMapping("/roles/{roleCode}/permissions")
    public GeneralResponse<RolePermissionsResponse> replaceRolePermissions(@PathVariable String roleCode,
            @RequestBody UpdateRolePermissionsRequest request) throws ResourceNotFoundException {
        GeneralResponse<RolePermissionsResponse> response = new GeneralResponse<>();
        response.setData(rbacService.replaceRolePermissions(roleCode, request));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Role permissions updated.");
        return response;
    }
}
