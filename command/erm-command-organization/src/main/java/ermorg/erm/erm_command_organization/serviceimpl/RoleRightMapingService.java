package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.erm_command_organization.dto.requestDTO.RightMappingRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRightMappingRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleRightMappingResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Right;
import ermorg.erm.erm_command_organization.model.Role;
import ermorg.erm.erm_command_organization.model.RoleRight;
import ermorg.erm.erm_command_organization.repository.RightRepository;
import ermorg.erm.erm_command_organization.repository.RoleRepository;
import ermorg.erm.erm_command_organization.repository.RoleRightMappingRepository;
import ermorg.erm.erm_command_organization.service.IRoleRightMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor  // ✅ Constructor injection — no @Autowired needed
public class RoleRightMapingService implements IRoleRightMappingService {

    private final RoleRightMappingRepository roleRightRepository;
    private final RoleRepository roleRepository;
    private final RightRepository rightRepository;

    @Override
    @Transactional  // ✅ Delete + Save is now atomic — no partial state on failure
    public RoleRightMappingResponse map(RoleRightMappingRequest request) throws ResourceNotFoundException {

        // 1. Validate role exists
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with ID: " + request.getRoleId()));

        // 2. Extract requested right IDs
        List<Long> requestedRightIds = request.getRights().stream()
                .map(RightMappingRequest::getRightId)
                .toList(); 

        // 3. Fetch all rights in ONE query and index by ID for O(1) lookup
        //    ✅ Fixes O(n²) stream filter inside loop
        Map<Long, Right> rightsById = rightRepository.findAllByRightIds(requestedRightIds)
                .stream()
                .collect(Collectors.toMap(Right::getId, Function.identity()));

        // 4. Validate ALL rights exist BEFORE touching existing data
        //    ✅ Fixes the bug where delete happens before validation
        List<Long> missingIds = requestedRightIds.stream()
                .filter(id -> !rightsById.containsKey(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Rights not found with IDs: " + missingIds);
        }

        // 5. Delete existing mappings for this role (only if any exist)
        List<RoleRight> existingMappings = roleRightRepository.findByRoleId(request.getRoleId());
        if (!existingMappings.isEmpty()) {
            roleRightRepository.deleteAll(existingMappings);
            // No manual flush needed — @Transactional handles ordering
        }

        // 6. Build new mappings cleanly
        List<RoleRight> newMappings = request.getRights().stream()
                .map(rightReq -> buildRoleRight(role, rightsById.get(rightReq.getRightId()), rightReq))
                .toList();

        // 7. Persist and return
        List<RoleRight> savedMappings = roleRightRepository.saveAll(newMappings);
        log.info("Mapped {} rights to roleId={}", savedMappings.size(), request.getRoleId());

        return new RoleRightMappingResponse(savedMappings, request.getRoleId());
    }

    @Override
    public RoleRightMappingResponse getMapping(Long roleId) {
        List<RoleRight> roleRights = roleRightRepository.findByRoleId(roleId);
        return new RoleRightMappingResponse(roleRights, roleId);
    }

    // ---- Private helper: replaces manual field-by-field mapping in the loop ----
    private RoleRight buildRoleRight(Role role, Right right, RightMappingRequest req) {
        RoleRight roleRight = new RoleRight();
        roleRight.setRole(role);
        roleRight.setRight(right);
        roleRight.setView(req.isView());
        roleRight.setCreate(req.isCreate());
        roleRight.setUpdate(req.isUpdate());
        roleRight.setDelete(req.isDelete());
        roleRight.setExport(req.isExport());
        roleRight.setPrint(req.isPrint());
        roleRight.setApprove(req.isApprove());
        roleRight.setReject(req.isReject());
        roleRight.setCancel(req.isCancel());
        return roleRight;
    }
}