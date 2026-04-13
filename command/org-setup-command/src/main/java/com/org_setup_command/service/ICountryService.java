package com.org_setup_command.service;

import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.Country;

public interface ICountryService {
	
	public Country getCountry(Long countryId) throws ResourceNotFoundException;

}
