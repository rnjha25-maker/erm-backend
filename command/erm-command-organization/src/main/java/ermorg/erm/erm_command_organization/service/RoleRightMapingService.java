package ermorg.erm.erm_command_organization.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.RightMappingRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRightMappingRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleRightMappingResponse;
import ermorg.erm.erm_command_organization.model.Right;
import ermorg.erm.erm_command_organization.model.Role;
import ermorg.erm.erm_command_organization.model.RoleRight;
import ermorg.erm.erm_command_organization.repository.RightRepository;
import ermorg.erm.erm_command_organization.repository.RoleRepository;
import ermorg.erm.erm_command_organization.repository.RoleRightMappingRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoleRightMapingService implements IRoleRightMappingService {

	@Autowired
	private RoleRightMappingRepository roleRightRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private RightRepository rightRepository;
	@Override
	public RoleRightMappingResponse map(RoleRightMappingRequest roleRightMappingRequest) {
		Role role = roleRepository.findById(roleRightMappingRequest.getRoleId()).orElseThrow(() -> new IllegalArgumentException("Role not found."));
		List<Right> rights = rightRepository.findAllByRightIds(roleRightMappingRequest
				.getRights()
				.stream()
				.map(right -> right.getRightId()).collect(Collectors.toList()));
		
		List<RoleRight> roleRights = roleRightRepository.findByRoleId(roleRightMappingRequest.getRoleId());
		
		if(roleRights.size() > 0) {
			
			roleRightRepository.deleteAll(roleRights);
			roleRightRepository.flush();
			
		}
		List<RightMappingRequest> rightRequest = roleRightMappingRequest.getRights();
		
		List<RoleRight> roleRightToSave = new ArrayList<>();
		for(RightMappingRequest right : rightRequest) {
			RoleRight roleRight = new RoleRight();
			roleRight.setRole(role);
			Right rightToSave = rights.stream().filter(r -> r.getId() == right.getRightId()).findFirst().orElseThrow(() -> new IllegalArgumentException("Right not found."));
			roleRight.setRight(rightToSave);
			roleRight.setView(right.isView());
			roleRight.setCreate(right.isCreate());
			roleRight.setUpdate(right.isUpdate());
			roleRight.setDelete(right.isDelete());
			roleRight.setExport(right.isExport());
			roleRight.setPrint(right.isPrint());
			roleRight.setApprove(right.isApprove());
			roleRight.setReject(right.isReject());
			roleRight.setCancel(right.isCancel());
			roleRightToSave.add(roleRight);
		}
		

		List<RoleRight> savedAllRoleRights = roleRightRepository.saveAll(roleRightToSave);
		
		return new RoleRightMappingResponse(savedAllRoleRights, roleRightMappingRequest.getRoleId());
		
	}
	
	@Override
	public RoleRightMappingResponse getMapping(Long roleId) {
		List<RoleRight> roleRights = roleRightRepository.findByRoleId(roleId);
		
		return new RoleRightMappingResponse(roleRights, roleId);
	}

}
