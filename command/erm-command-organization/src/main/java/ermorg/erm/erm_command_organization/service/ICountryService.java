package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.responseDTO.CountryResponse;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.model.Country;

import java.util.List;

public interface ICountryService {
    List<CountryResponse> getAllCountry();
    Country createCountry(String name, String code);
    Country updateCountry(Long id, String name, String code) throws DataNotFoundException;
    void deleteCountry(Long id) throws InvalidDataException;
}
