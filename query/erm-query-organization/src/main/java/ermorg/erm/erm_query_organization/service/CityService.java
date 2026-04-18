package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.City;
import ermorg.erm.erm_query_organization.model.State;
import ermorg.erm.erm_query_organization.repository.CityRepository;
import ermorg.erm.erm_query_organization.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CityService implements ICityService{

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private StateRepository stateRepository;

    @Override
    public List<City> getAllCity(Long stateId) throws DataNotFoundException {
        Optional<State> stateOptional = stateRepository.findById(stateId);
        if(stateOptional.isEmpty()){
            throw new DataNotFoundException("Invalid State ID");
        }
        return cityRepository.findByStateId(stateId);
    }
}
