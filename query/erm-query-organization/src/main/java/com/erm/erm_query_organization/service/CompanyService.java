package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Company;
import com.erm.erm_query_organization.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService implements ICompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public Company getCompanyById(Long id) throws DataNotFoundException{
        Optional<Company> companyOptional = companyRepository.findById(id);
        if(companyOptional.isEmpty()){
            throw new DataNotFoundException("Company not found");
        }
        return companyOptional.get();
    }

    @Override
    public List<Company> getAllCompanies(){
        return companyRepository.findAll();
    }

}
