package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.responseDTO.CountryResponse;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.model.Country;
import com.erm.erm_command_organization.repository.CountryRepository;
import com.erm.erm_command_organization.util.AuditorAwareImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryService implements ICountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AuditorAware auditor;

    @Override
    public List<CountryResponse> getAllCountry(){
        return countryRepository.findAll()
                .stream().filter(country -> !country.getDeleted())
                .map(country -> new CountryResponse(country))
                .collect(Collectors.toList());
    }

    @Override
    public Country createCountry(String name, String code){
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
        Country country = new Country();
        country.setName(name);
        country.setCode(code);
        country.setClientIP(clientIp);
        return countryRepository.save(country);
    }

    @Override
    public Country updateCountry(Long id, String name, String code) throws DataNotFoundException{
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
        Optional<Country> countryOptional = countryRepository.findById(id);
        if(countryOptional.isEmpty()){
            throw new DataNotFoundException("Country not found");
        }
        Country country = countryOptional.get();
        country.setName(name);
        country.setCode(code);
        country.setClientIP(clientIp);
        return countryRepository.save(country);
    }

    @Override
    public void deleteCountry(Long id) throws InvalidDataException{
        Optional<Country> countryOptional = countryRepository.findById(id);
        if(countryOptional.isEmpty()){
            throw new InvalidDataException("Invalid Country ID");
        }
        countryRepository.deleteById(id);
    }

}
