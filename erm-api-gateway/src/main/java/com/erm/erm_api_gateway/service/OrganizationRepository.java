package com.erm.erm_api_gateway.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.erm_api_gateway.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
