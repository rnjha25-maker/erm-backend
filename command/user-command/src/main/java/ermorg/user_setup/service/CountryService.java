package ermorg.user_setup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Country;
import ermorg.user_setup.repository.CountryRepository;

@Service
public class CountryService implements ICountryService {

	@Autowired
	private CountryRepository countryRepository;
	public CountryService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Country getCountry(Long countryId) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		return countryRepository.findById(countryId).orElseThrow(()-> new ResourceNotFoundException("Country not found."));
	}

}
