package com.org_setup_command.service;

import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.State;

public interface IStateService {

	public State getState(Long stateId) throws ResourceNotFoundException;
}
