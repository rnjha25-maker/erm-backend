package com.user_setup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.City;
import com.user_setup.repository.CityRepository;

@Service
public class CityService implements ICityService {

	@Autowired
	private CityRepository cityRepository;


//	@Cache(key="cityId", value="city")
	@Override
	public City getCity(Long cityId) throws ResourceNotFoundException {
		return cityRepository.findById(cityId).orElseThrow(()->new ResourceNotFoundException(""));
	}

}
