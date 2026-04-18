package ermorg.user_setup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.City;
import ermorg.user_setup.repository.CityRepository;

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
