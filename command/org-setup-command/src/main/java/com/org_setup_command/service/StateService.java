package com.org_setup_command.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.State;
import com.org_setup_command.repository.StateRepository;

@Service
public class StateService implements IStateService {

	@Autowired
	private StateRepository stateRepository;
	
	@Override
	public State getState(Long stateId) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		return stateRepository.findById(stateId).orElseThrow(()-> new ResourceNotFoundException("State/Province not available."));
	}

}
