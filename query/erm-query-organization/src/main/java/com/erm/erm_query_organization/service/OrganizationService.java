package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.dto.OrganizationDTO;
import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Organization;
import com.erm.erm_query_organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationService implements IOrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;

    @Override
    public Organization getOrganizationById(Long id) throws DataNotFoundException {
        Optional<Organization> organizationOptional = organizationRepository.findById(id);
        if(organizationOptional.isEmpty()){
            throw new DataNotFoundException("Organization not found.");
        }
        return organizationOptional.get();
    }

    @Override
    public List<OrganizationDTO> getAllOrganizations(){
        return organizationRepository.findAll().stream().map(org -> new OrganizationDTO(org))
        		.sorted((org1, org2) -> Long.compare(org2.getId(), org1.getId()))
        		.collect(Collectors.toList());
    }

}
