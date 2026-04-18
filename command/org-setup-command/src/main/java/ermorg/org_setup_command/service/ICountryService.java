package ermorg.org_setup_command.service;

import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.modal.Country;

public interface ICountryService {
	
	public Country getCountry(Long countryId) throws ResourceNotFoundException;

}
