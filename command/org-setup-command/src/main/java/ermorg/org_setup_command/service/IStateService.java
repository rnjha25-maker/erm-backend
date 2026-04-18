package ermorg.org_setup_command.service;

import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.modal.State;

public interface IStateService {

	public State getState(Long stateId) throws ResourceNotFoundException;
}
