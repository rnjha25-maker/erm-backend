package com.erm.erm_command_organization.service;

import java.util.List;
import java.util.Map;

import com.erm.erm_command_organization.dto.request.UpdateModuleRequest;
import com.erm.erm_command_organization.dto.requestDTO.ModuleRightRequest;
import com.erm.erm_command_organization.dto.requestDTO.OrganizationDTO;
import com.erm.erm_command_organization.dto.responseDTO.OrganizationResponse;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

import jakarta.validation.Valid;

public interface IOrganizationService {
	OrganizationResponse createOrganization(OrganizationDTO request) throws ResourceNotFoundException;
	OrganizationResponse updateOrganization(OrganizationDTO request) throws DataNotFoundException;
    void deleteOrganization(Long id) throws InvalidDataException;
	List<OrganizationResponse> getAllOrganization();
	OrganizationResponse getOrganization(Long id, int back)throws DataNotFoundException;
	List<Map<String, Object>> getAllModules();
	public UpdateModuleRequest updateModule(UpdateModuleRequest request);
	UpdateModuleRequest updateModuleView(UpdateModuleRequest request)throws ResourceNotFoundException;
	ModuleRightRequest updateRight(ModuleRightRequest request)throws ResourceNotFoundException;
	
    
    
}
