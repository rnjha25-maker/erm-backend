package com.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Organization save(Organization organization);

	List<Organization> findAllByCreatedAtBetween(Date startOfWeek, Date endOfWeek);
	
	// JOIN FETCH queries to prevent N+1 queries when accessing related entities
	@Query("SELECT o FROM Organization o JOIN FETCH o.companies WHERE o.id = :id AND o.deleted = false")
	Organization getOrganizationWithCompanies(@Param("id") Long id);
	
	@Query("SELECT o FROM Organization o JOIN FETCH o.modules WHERE o.id = :id AND o.deleted = false")
	Organization getOrganizationWithModules(@Param("id") Long id);
	
	@Query("SELECT o FROM Organization o JOIN FETCH o.companies LEFT JOIN FETCH o.departments WHERE o.id = :id AND o.deleted = false")
	Organization getOrganizationWithAllData(@Param("id") Long id);
}
