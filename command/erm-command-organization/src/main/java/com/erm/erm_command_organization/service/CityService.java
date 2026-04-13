package com.erm.erm_command_organization.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import com.erm.erm_command_organization.dto.request.CityDto;
import com.erm.erm_command_organization.dto.request.CityRequest;
import com.erm.erm_command_organization.dto.responseDTO.CityResponse;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.model.City;
import com.erm.erm_command_organization.model.State;
import com.erm.erm_command_organization.repository.CityRepository;
import com.erm.erm_command_organization.repository.StateRepository;
import com.erm.erm_command_organization.util.AuditorAwareImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CityService implements ICityService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private AuditorAware auditor;

    @Override
    public List<CityResponse> getAllCities(Long stateId) throws ResourceNotFoundException {
        State stateData = stateRepository.findById(stateId).filter(state -> !state.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("State not found."));
        return stateData.getCities().stream().map(CityResponse::new).collect(Collectors.toList());
    }

    @Override
    public City createCity(String name, Long StateId) throws InvalidDataException{
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Optional<State> stateOptional = stateRepository.findById(StateId);
        if(stateOptional.isEmpty()){
            throw new InvalidDataException("Invalid State ID");
        }
        State state = stateOptional.get();

        City city = new City();
        city.setName(name);
        city.setState(state);
        city.setClientIP(clientIp);
        return cityRepository.save(city);
    }

    @Override
    public City updateCity(Long id, String name, Long StateId) throws DataNotFoundException, InvalidDataException{
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Optional<City> cityOptional = cityRepository.findById(id);
        if(cityOptional.isEmpty()){
            throw new DataNotFoundException("City not found");
        }
        City city = cityOptional.get();

        Optional<State> stateOptional = stateRepository.findById(StateId);
        if(stateOptional.isEmpty()){
            throw new InvalidDataException("Invalid State ID");
        }
        State state = stateOptional.get();

        city.setName(name);
        city.setState(state);
        city.setClientIP(clientIp);
        return cityRepository.save(city);
    }

    @Override
    public void deleteCity(Long id) throws InvalidDataException{
        Optional<City> cityOptional = cityRepository.findById(id);
        if(cityOptional.isEmpty()){
            throw new InvalidDataException("Invalid City ID");
        }
        cityRepository.deleteById(id);
    }

	@Override
	public void saveCities(CityRequest request) {
		List<CityDto> cities = request.getCities();
		
		for(CityDto cityDto : cities) {
			
			Optional<State> stateOptional = stateRepository.findById(cityDto.getStateId());
			
			if(!stateOptional.isPresent()) {
				log.info("State not found {}", cityDto.getStateId());
				continue;
			}
			State state = stateOptional.get();
			City city = new City();
			city.setDeleted(false);
			city.setName(cityDto.getName());
			city.setState(state);
			
			cityRepository.save(city);
			log.info("City saved successfully {}", city.getName());
		}
		
	}
    
    
}
