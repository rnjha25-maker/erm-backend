package com.user_setup.service;

import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.State;

public interface IStateService {

	public State getState(Long stateId) throws ResourceNotFoundException;
}
