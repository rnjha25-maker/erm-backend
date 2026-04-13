package com.user_setup.service;

import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.Country;

public interface ICountryService {
	
	public Country getCountry(Long countryId) throws ResourceNotFoundException;

}
