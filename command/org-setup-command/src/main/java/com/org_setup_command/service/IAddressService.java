package com.org_setup_command.service;

import com.org_setup_command.exception.ResourceNotFoundException;

public interface IAddressService {
    void createAddress(String address, Long cityId, Long stateId, Long countryId) throws InvalidDataException;
    void updateAddress(Long id, String address, Long cityId, Long stateId, Long countryId) throws ResourceNotFoundException, InvalidDataException;
    void deleteAddress(Long id) throws InvalidDataException;
}
