package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Role;
import ermorg.erm.erm_command_organization.model.history.RoleHistory;
import ermorg.erm.erm_command_organization.repository.RoleRepository;
import ermorg.erm.erm_command_organization.repository.history.RoleHistoryRepository;
import ermorg.erm.erm_command_organization.service.IRoleService;

@Service
public class RoleService implements IRoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleHistoryRepository roleHistoryRepository;

	@Override
	public RoleResponse saveRole(RoleRequest roleRequest) throws ResourceNotFoundException {
		Role role;
		boolean isUpdate = roleRequest.getRoleId() > 0;
		String requestedRoleCode = toRoleCode(roleRequest.getRoleName());

		if (isUpdate) {
			role = roleRepository.findById(roleRequest.getRoleId())
					.filter(r -> !r.getDeleted())
					.orElseThrow(() -> new ResourceNotFoundException("Role not found."));

			roleRepository.findByNameAndDeletedFalseAndIdNot(roleRequest.getRoleName(), role.getId())
					.ifPresent(r -> {
						throw new IllegalArgumentException("Role with this name already exists.");
					});

			if (role.getRoleCode() != null && !role.getRoleCode().equals(requestedRoleCode)
					&& roleRepository.existsByRoleCodeIgnoreCase(requestedRoleCode)) {
				throw new IllegalArgumentException("Role code already exists.");
			}

			saveRoleHistory(role, "U");
		} else {
			roleRepository.findByNameAndDeletedFalse(roleRequest.getRoleName())
					.ifPresent(r -> {
						throw new IllegalArgumentException("Role with this name already exists.");
					});

			if (roleRepository.existsByRoleCodeIgnoreCase(requestedRoleCode)) {
				throw new IllegalArgumentException("Role code already exists.");
			}

			role = new Role();
		}

		role.setName(roleRequest.getRoleName());
		role.setNormalizedName(normalizeName(roleRequest.getRoleName()));
		if (role.getRoleCode() == null || role.getRoleCode().isBlank()) {
			role.setRoleCode(requestedRoleCode);
		}
		role.setPriority(roleRequest.getPriority());
		role.setDescription(roleRequest.getDescription());

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
		return roleRepository.findAll().stream()
				.filter(r -> !r.getDeleted() && !"orgAdmin".equals(r.getName()))
				.map(RoleResponse::new)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteRole(long roleId) throws ResourceNotFoundException {
		Role role = roleRepository.findById(roleId).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found."));

		role.setDeleted(true);
		roleRepository.save(role);
		saveRoleHistory(role, "D");
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

	private String normalizeName(String value) {
		return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase();
	}

	private String toRoleCode(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Role name is required.");
		}
		return value.trim()
				.replaceAll("[^A-Za-z0-9]+", "_")
				.replaceAll("(^_+|_+$)", "")
				.toUpperCase();
	}
}
