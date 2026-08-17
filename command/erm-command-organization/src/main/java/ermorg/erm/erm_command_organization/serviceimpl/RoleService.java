package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Role;
import ermorg.erm.erm_command_organization.model.RoleType;
import ermorg.erm.erm_command_organization.model.history.RoleHistory;
import ermorg.erm.erm_command_organization.repository.RoleRepository;
import ermorg.erm.erm_command_organization.repository.RoleTypeRepository;
import ermorg.erm.erm_command_organization.repository.history.RoleHistoryRepository;
import ermorg.erm.erm_command_organization.service.IRoleService;
import ermorg.erm.erm_command_organization.service.RoleTypeValidator;

@Service
public class RoleService implements IRoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleTypeRepository roleTypeRepository;

	@Autowired
	private RoleTypeValidator roleTypeValidator;

	@Autowired
	private RoleHistoryRepository roleHistoryRepository;

	@Override
	public RoleResponse saveRole(RoleRequest roleRequest) throws ResourceNotFoundException {

	    Role role;

	    boolean isUpdate = roleRequest.getRoleId() > 0;

	    if (isUpdate) {
	        role = roleRepository.findById(roleRequest.getRoleId())
	                .filter(r -> !r.getDeleted())
	                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

	        // ✅ Check duplicate name for update (excluding current ID)
	        roleRepository.findByNameAndDeletedFalseAndIdNot(roleRequest.getRoleName(), role.getId())
	                .ifPresent(r -> {
	                    throw new IllegalArgumentException("Role with this name already exists.");
	                });

	        // Save history before update
	        saveRoleHistory(role, "U");

	    } else {
	        // ✅ Check duplicate name for create
	        roleRepository.findByNameAndDeletedFalse(roleRequest.getRoleName())
	                .ifPresent(r -> {
	                    throw new IllegalArgumentException("Role with this name already exists.");
	                });

	        role = new Role();
	    }

	    // Set basic fields
	    role.setName(roleRequest.getRoleName());
	    role.setPriority(roleRequest.getPriority());
	    role.setDescription(roleRequest.getDescription());

	    // Validate and set RoleType if provided
		if (roleRequest.getRoleTypeCode() != null && !roleRequest.getRoleTypeCode().trim().isEmpty()) {
			Optional<RoleType> roleTypeOpt = roleTypeValidator.validateRoleType(roleRequest.getRoleTypeCode());

			roleTypeOpt.ifPresent(role::setRoleType);

			// Optional: handle case when not found
			if (roleTypeOpt.isEmpty()) {
				throw new IllegalArgumentException("Invalid role type code.");
			}

		} else if (!isUpdate) {
			// For new roles, roleType is required
			throw new IllegalArgumentException("Role type code is required for new roles.");
		}
	    // For update, if roleTypeCode is not provided, keep existing roleType

	    Role savedRole = roleRepository.save(role);

	    return new RoleResponse(savedRole);
	}

	@Override
	public RoleResponse getRole(long roleId) throws ResourceNotFoundException {
		Role role = roleRepository.findById(roleId).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found."));
		return new RoleResponse(role);
	}

	@Override
	public List<RoleResponse> getAllRoles() {
		return roleRepository.findAll().stream().filter(r -> !r.getDeleted() && !r.getName().equals("orgAdmin"))
				.map(RoleResponse::new).collect(Collectors.toList());
	}

	@Override
	public void deleteRole(long roleId) throws ResourceNotFoundException {
		Role role = roleRepository.findById(roleId).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found."));

		role.setDeleted(true);
		roleRepository.save(role);

		saveRoleHistory(role, "D");

	}

	@Override
	public List<RoleType> getRoleType() throws ResourceNotFoundException {
		return roleTypeRepository.findAll();
	}

	private void saveRoleHistory(Role role, String operation) {

		RoleHistory roleHistory = new RoleHistory();
		roleHistory.setDeleted(role.getDeleted());
		roleHistory.setRoleId(role.getId());
		roleHistory.setRoleName(role.getName());
		roleHistory.setPriority(role.getPriority());
		roleHistory.setDescription(role.getDescription());
		roleHistory.setOperation(operation);

		roleHistoryRepository.save(roleHistory);

	}

}
