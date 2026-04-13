package com.erm.erm_command_organization.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erm.erm_command_organization.dto.requestDTO.RightDto;
import com.erm.erm_command_organization.dto.responseDTO.RightResponse;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.model.Modules;
import com.erm.erm_command_organization.model.Right;
import com.erm.erm_command_organization.model.history.RightHistory;
import com.erm.erm_command_organization.repository.ModuleRepository;
import com.erm.erm_command_organization.repository.RightRepository;
import com.erm.erm_command_organization.repository.history.RightHistoryRepository;

@Service
public class RightService implements IRightService {

	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private RightRepository rightRepository;
	
	@Autowired
	private RightHistoryRepository rightHistoryRepository;
	@Override
	public List<RightResponse> getAllRights() {
		List<RightResponse> rights = rightRepository.findAll().stream().filter(r -> !r.getDeleted()).map(right -> new RightResponse(right)).collect(Collectors.toList());
		
		return rights;
	}

	@Override
	public RightResponse getRightById(long rightId) throws ResourceNotFoundException {
		
		Right right = rightRepository.findById(rightId).filter(r -> !r.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Right not found."));
		return new RightResponse(right);
	}

	@Override
	public RightResponse saveRight(RightDto request) throws ResourceNotFoundException {
		Modules module = moduleRepository.findById(request.getModuleId()).filter(m -> !m.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Module not found."));
		
		RightResponse response = new RightResponse();
		List<Right> rights = module.getRights();
		AtomicBoolean isUpdate = new AtomicBoolean(false);
		if(request.getRightId() > 0) {
			rights.stream().filter(r -> r.getId() == request.getRightId()).findFirst().ifPresent(r ->{
				r.setName(request.getRightName());
				r.setDescription(request.getRightDescription());
				
				response.setRightId(r.getId());
				response.setRightName(r.getName());
				response.setRightDescription(r.getDescription());
				response.setModuleId(module.getId());
				isUpdate.set(false);
			});
		}else {
			Right right = new Right();
			right.setName(request.getRightName());
			right.setDescription(request.getRightDescription());
			right.setModule(module);
			rights.add(right);
			
			
		}
		Modules saveModule = moduleRepository.save(module);
		List<Right> savedRights = saveModule.getRights();
		
		Right right = savedRights.get(savedRights.size() - 1);
		
		
		if(isUpdate.get()) {
			saveHistory(rights.get(rights.size() - 1), "U");

		}else {
			response.setRightId(right.getId());
			response.setRightName(right.getName());
			response.setRightDescription(right.getDescription());
			response.setModuleId(module.getId());
		}
		return response;
	}

	@Override
	public void deleteRight(long rightId) throws ResourceNotFoundException {
		
		Right right = rightRepository.findById(rightId).filter(r -> !r.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Right not found."));
		right.setDeleted(true);
		rightRepository.save(right);
		
		saveHistory(right, "D");
		
	}
	
	private void saveHistory(Right right, String operation) {
		
		RightHistory rightHistory = new RightHistory();
		copyFromRight(right, rightHistory);
		rightHistory.setOperation(operation);
		rightHistoryRepository.save(rightHistory);
	}

	private void copyFromRight(Right right, RightHistory rightHistory) {
		
		rightHistory.setRightId(right.getId());
		rightHistory.setRightName(right.getName());
		rightHistory.setDescription(right.getDescription());
		rightHistory.setModuleId(right.getModule().getId());
		
		
	}
	
	

}
