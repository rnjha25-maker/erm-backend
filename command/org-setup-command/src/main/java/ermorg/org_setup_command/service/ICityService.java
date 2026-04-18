package ermorg.org_setup_command.service;

import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.modal.City;

public interface ICityService {
//    void createCity(String name, Long StateId) throws InvalidDataException;
//    void updateCity(Long id, String name, Long StateId) throws ResourceNotFoundException, InvalidDataException;
//    void deleteCity(Long id) throws InvalidDataException;
    public City getCity(Long cityId)throws ResourceNotFoundException;
}
