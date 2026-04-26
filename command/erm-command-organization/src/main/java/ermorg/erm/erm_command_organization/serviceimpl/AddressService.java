package ermorg.erm.erm_command_organization.serviceimpl;

import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.model.Address;
import ermorg.erm.erm_command_organization.model.City;
import ermorg.erm.erm_command_organization.model.Country;
import ermorg.erm.erm_command_organization.model.State;
import ermorg.erm.erm_command_organization.repository.AddressRepository;
import ermorg.erm.erm_command_organization.repository.CityRepository;
import ermorg.erm.erm_command_organization.repository.CountryRepository;
import ermorg.erm.erm_command_organization.repository.StateRepository;
import ermorg.erm.erm_command_organization.service.IAddressService;
import ermorg.erm.erm_command_organization.util.AuditorAwareImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    @Autowired
    private AuditorAware auditor;

    @Override
    public Address createAddress(String address, Long cityId, Long stateId, Long countryId) throws InvalidDataException {
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
        Country country = getCountry(countryId);
        State state = getState(stateId);
        City city = getCity(cityId);

        Address addressEntity = new Address();
        addressEntity.setAddress(address);
        addressEntity.setCity(city);
        addressEntity.setCountry(country);
        addressEntity.setState(state);
        addressEntity.setClientIP(clientIp);
        return addressRepository.save(addressEntity);
    }

    @Override
    public Address updateAddress(Long id, String address, Long cityId, Long stateId, Long countryId) throws DataNotFoundException, InvalidDataException{
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Optional<Address> addressOptional = addressRepository.findById(id);
        if(addressOptional.isEmpty()){
            throw new DataNotFoundException("Address not found!");
        }
        Address addressEntity = addressOptional.get();

        Country country = getCountry(countryId);
        State state = getState(stateId);
        City city = getCity(cityId);

        addressEntity.setAddress(address);
        addressEntity.setCity(city);
        addressEntity.setCountry(country);
        addressEntity.setState(state);
        addressEntity.setClientIP(clientIp);
        return addressRepository.save(addressEntity);
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
