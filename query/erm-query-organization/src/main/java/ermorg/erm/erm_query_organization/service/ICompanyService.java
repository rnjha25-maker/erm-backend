package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.Company;

import java.util.List;

public interface ICompanyService {
    Company getCompanyById(Long id) throws DataNotFoundException;
    List<Company> getAllCompanies();
}
