package com.erm.erm_command_organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.erm_command_organization.model.history.OrganizationHistory;

@Repository
public interface OrganizationHistoryRepository extends JpaRepository<OrganizationHistory, Long>{

	@Query("SELECT e FROM OrganizationHistory e WHERE e.organizationId = :organizationId ORDER BY e.createdAt DESC LIMIT 1")
	OrganizationHistory findLastModified(@Param("organizationId") Long organizationId);

}
