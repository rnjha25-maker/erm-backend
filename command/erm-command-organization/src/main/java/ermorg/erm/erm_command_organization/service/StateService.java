package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.responseDTO.StateResponse;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Country;
import ermorg.erm.erm_command_organization.model.State;
import ermorg.erm.erm_command_organization.repository.CountryRepository;
import ermorg.erm.erm_command_organization.repository.StateRepository;
import ermorg.erm.erm_command_organization.util.AuditorAwareImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StateService implements IStateService {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AuditorAware auditor;

    @Override
    public List<StateResponse> getAllStates(Long countryId) throws ResourceNotFoundException{
        Country countryData =  countryRepository.findById(countryId).filter(country -> !country.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found."));
        return countryData.getStates().stream().map(StateResponse::new).collect(Collectors.toList());
    }

    @Override
    public State createState(String name, Long countryId) throws InvalidDataException{
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Optional<Country> countryOptional = countryRepository.findById(countryId);
        if(countryOptional.isEmpty()){
            throw new InvalidDataException("Invalid Country ID");
        }
        Country country = countryOptional.get();

        State state = new State();
        state.setName(name);
        state.setCountry(country);
        state.setClientIP(clientIp);
        return stateRepository.save(state);
    }

    @Override
    public State updateState(Long id, String name, Long countryId) throws DataNotFoundException, InvalidDataException{
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Optional<State> stateOptional = stateRepository.findById(id);
        if(stateOptional.isEmpty()){
            throw new DataNotFoundException("State not found");
        }
        State state = stateOptional.get();

        Optional<Country> countryOptional = countryRepository.findById(countryId);
        if(countryOptional.isEmpty()){
            throw new InvalidDataException("Invalid Country ID");
        }
        Country country = countryOptional.get();

        state.setName(name);
        state.setCountry(country);
        state.setClientIP(clientIp);
        return stateRepository.save(state);
    }

    @Override
    public void deleteState(Long id) throws InvalidDataException{
        Optional<State> stateOptional = stateRepository.findById(id);
        if(stateOptional.isEmpty()){
            throw new InvalidDataException("Invalid State ID");
        }
        stateRepository.deleteById(id);
    }
}
