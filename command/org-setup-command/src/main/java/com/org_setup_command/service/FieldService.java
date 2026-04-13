package com.org_setup_command.service;

import java.util.List;

import com.org_setup_command.dto.request.FieldRequestDTO;
import com.org_setup_command.dto.response.ModuleResponse;
import com.org_setup_command.dto.response.OrganizationFieldsDTO;
import com.org_setup_command.exception.ResourceNotFoundException;

public interface FieldService {
	
	public ModuleResponse saveField(FieldRequestDTO request) throws ResourceNotFoundException;

	public void deleteField(Long fieldId) throws ResourceNotFoundException;
	
	public OrganizationFieldsDTO getField(Long fieldId) throws ResourceNotFoundException;
	
	public List<OrganizationFieldsDTO> getAllField(Long organizationId);

	public List<String> getSystemFields(long organizationId) throws ResourceNotFoundException;

}
