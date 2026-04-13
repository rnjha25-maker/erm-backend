package com.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.storage.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
