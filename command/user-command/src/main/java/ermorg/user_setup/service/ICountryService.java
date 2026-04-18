package ermorg.user_setup.service;

import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Country;

public interface ICountryService {
	
	public Country getCountry(Long countryId) throws ResourceNotFoundException;

}
