package com.user_setup.service;

import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.City;

public interface ICityService {

	public City getCity(Long cityId) throws ResourceNotFoundException;
}
