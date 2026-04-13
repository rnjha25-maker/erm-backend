package com.org_setup_command.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.Address;
import com.org_setup_command.modal.City;
import com.org_setup_command.modal.Country;
import com.org_setup_command.modal.State;
import com.org_setup_command.repository.AddressRepository;
import com.org_setup_command.repository.CityRepository;
import com.org_setup_command.repository.CountryRepository;
import com.org_setup_command.repository.StateRepository;
import com.org_setup_command.util.AuditorAwareImpl;

@Service
public class AddressService implements IAddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepository;

//    @Autowired
//    private AuditorAware auditor;

    @Override
    public void createAddress(String address, Long cityId, Long stateId, Long countryId) throws InvalidDataException{
//        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
        Country country = getCountry(countryId);
        State state = getState(stateId);
        City city = getCity(cityId);

        Address addressEntity = new Address();
        addressEntity.setAddress(address);
        addressEntity.setCity(city);
        addressEntity.setCountry(country);
        addressEntity.setState(state);
//        addressEntity.setClientIP(clientIp);
         addressRepository.save(addressEntity);
    }

    @Override
    public void updateAddress(Long id, String address, Long cityId, Long stateId, Long countryId) throws ResourceNotFoundException, InvalidDataException{
//        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Optional<Address> addressOptional = addressRepository.findById(id);
        if(addressOptional.isEmpty()){
            throw new ResourceNotFoundException("Address not found!");
        }
        Address addressEntity = addressOptional.get();

        Country country = getCountry(countryId);
        State state = getState(stateId);
        City city = getCity(cityId);

        addressEntity.setAddress(address);
        addressEntity.setCity(city);
        addressEntity.setCountry(country);
        addressEntity.setState(state);
//        addressEntity.setClientIP(clientIp);
         addressRepository.save(addressEntity);
    }

    @Override
    public void deleteAddress(Long id) throws InvalidDataException {
        Optional<Address> addressOptional = addressRepository.findById(id);
        if(addressOptional.isEmpty()){
            throw new InvalidDataException("Invalid Address ID");
        }
        addressRepository.deleteById(id);
    }

    private Country getCountry(Long id) throws InvalidDataException {
        Optional<Country> countryOptional = countryRepository.findById(id);
        if(countryOptional.isEmpty()){
            throw new InvalidDataException("Invalid Country ID");
        }
        return countryOptional.get();
    }

    private State getState(Long id) throws InvalidDataException {
        Optional<State> stateOptional = stateRepository.findById(id);
        if(stateOptional.isEmpty()){
            throw new InvalidDataException("Invalid State ID");
        }
        return stateOptional.get();
    }

    private City getCity(Long id) throws InvalidDataException {
        Optional<City> cityOptional = cityRepository.findById(id);
        if(cityOptional.isEmpty()){
            throw new InvalidDataException("Invalid City ID");
        }
        return cityOptional.get();
    }
}
