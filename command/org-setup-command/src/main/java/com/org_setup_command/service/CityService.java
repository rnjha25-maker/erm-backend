package com.org_setup_command.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.City;
import com.org_setup_command.modal.State;
import com.org_setup_command.repository.CityRepository;
import com.org_setup_command.repository.StateRepository;
import com.org_setup_command.util.AuditorAwareImpl;

@Service
public class CityService implements ICityService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private StateRepository stateRepository;

   


//	@Cache(key="cityId", value="city")
	@Override
	public City getCity(Long cityId) throws ResourceNotFoundException {
		return cityRepository.findById(cityId).orElseThrow(()->new ResourceNotFoundException(""));
	}
}
