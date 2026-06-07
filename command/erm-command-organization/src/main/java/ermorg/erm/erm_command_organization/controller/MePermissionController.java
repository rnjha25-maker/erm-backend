package ermorg.erm.erm_command_organization.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.dto.responseDTO.UserPermissionsResponse;
import ermorg.erm.erm_command_organization.serviceimpl.RbacService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MePermissionController {

    private final RbacService rbacService;

    @GetMapping("/permissions")
    public GeneralResponse<UserPermissionsResponse> getMyPermissions(@RequestHeader("X-User-Id") Long userId) {
        GeneralResponse<UserPermissionsResponse> response = new GeneralResponse<>();
        response.setData(rbacService.getUserPermissions(userId));
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }
}
