package ermorg.user_setup.service;

import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.City;

public interface ICityService {

	public City getCity(Long cityId) throws ResourceNotFoundException;
}
