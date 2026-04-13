package com.user_setup.service;

import com.user_setup.dto.request.ComponyDTO;
import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.Company;

public interface ICompanyService {

	public Company getCompany(Long componyId) throws ResourceNotFoundException;
	public void saveCompany(ComponyDTO request)throws ResourceNotFoundException;
	public void updateCompany(ComponyDTO request)throws ResourceNotFoundException;

}
