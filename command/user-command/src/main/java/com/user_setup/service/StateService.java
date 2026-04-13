package com.user_setup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.State;
import com.user_setup.repository.StateRepository;

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
