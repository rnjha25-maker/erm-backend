package ermorg.user_setup.service;

import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.State;

public interface IStateService {

	public State getState(Long stateId) throws ResourceNotFoundException;
}
