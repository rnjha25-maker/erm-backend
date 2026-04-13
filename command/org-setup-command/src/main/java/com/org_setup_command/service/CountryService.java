package com.org_setup_command.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.Country;
import com.org_setup_command.repository.CountryRepository;

@Service
public class CountryService implements ICountryService {

	@Autowired
	private CountryRepository countryRepository;
	

	@Override
	public Country getCountry(Long countryId) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		return countryRepository.findById(countryId).orElseThrow(()-> new ResourceNotFoundException("Country not found."));
	}

}
