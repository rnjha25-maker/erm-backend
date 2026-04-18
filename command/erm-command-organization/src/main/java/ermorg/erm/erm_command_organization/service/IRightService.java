package ermorg.erm.erm_command_organization.service;

import java.util.List;

import ermorg.erm.erm_command_organization.dto.requestDTO.RightDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.RightResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IRightService {
	
	public List<RightResponse> getAllRights();
	public RightResponse getRightById(long rightId) throws ResourceNotFoundException;
	public RightResponse saveRight(RightDto request) throws ResourceNotFoundException;
	public void deleteRight(long rightId) throws ResourceNotFoundException;

}
