package com.org_setup_command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company save(Company company);

    @Query("SELECT c FROM Company c WHERE c.organization.id = :organizationId AND c.deleted != true")
	List<Company> findAllCompaniesByOrganizationId(@Param("organizationId") Long organizationId);
}
