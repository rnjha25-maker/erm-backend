package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.model.Address;

public interface IAddressService {
    Address createAddress(String address, Long cityId, Long stateId, Long countryId) throws InvalidDataException;
    Address updateAddress(Long id, String address, Long cityId, Long stateId, Long countryId) throws DataNotFoundException, InvalidDataException;
    void deleteAddress(Long id) throws InvalidDataException;
}
