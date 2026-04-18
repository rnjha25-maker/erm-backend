package ermorg.user_setup.service;

import ermorg.user_setup.dto.request.ComponyDTO;
import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Company;

public interface ICompanyService {

	public Company getCompany(Long componyId) throws ResourceNotFoundException;
	public void saveCompany(ComponyDTO request)throws ResourceNotFoundException;
	public void updateCompany(ComponyDTO request)throws ResourceNotFoundException;

}
