package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Country;
import com.erm.erm_query_organization.model.State;
import com.erm.erm_query_organization.repository.CountryRepository;
import com.erm.erm_query_organization.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StateService implements IStateService {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public List<State> getAllState(Long countryId) throws DataNotFoundException {
        Optional<Country> countryOptional = countryRepository.findById(countryId);
        if(countryOptional.isEmpty()){
            throw new DataNotFoundException("Invalid Country ID");
        }
        return stateRepository.findByCountryId(countryId);
    }
}
