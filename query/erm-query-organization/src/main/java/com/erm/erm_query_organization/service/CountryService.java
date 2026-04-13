package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.model.Country;
import com.erm.erm_query_organization.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryService implements ICountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public List<Country> getAllCountry() {
        return countryRepository.findAll();
    }
}
