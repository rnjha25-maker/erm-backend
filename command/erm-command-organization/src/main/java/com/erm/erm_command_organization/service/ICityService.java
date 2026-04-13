package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.request.CityRequest;
import com.erm.erm_command_organization.dto.responseDTO.CityResponse;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.model.City;

import java.util.List;

public interface ICityService {
    List<CityResponse> getAllCities(Long stateId) throws ResourceNotFoundException;
    City createCity(String name, Long StateId) throws InvalidDataException;
    City updateCity(Long id, String name, Long StateId) throws DataNotFoundException, InvalidDataException;
    void deleteCity(Long id) throws InvalidDataException;
	void saveCities(CityRequest request);
}
