package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.responseDTO.StateResponse;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.model.State;

import java.util.List;

public interface IStateService {
    List<StateResponse> getAllStates(Long countryId) throws ResourceNotFoundException;
    State createState(String name, Long countryId) throws InvalidDataException;
    State updateState(Long id, String name, Long countryId) throws DataNotFoundException, InvalidDataException;
    void deleteState(Long id) throws InvalidDataException;
}