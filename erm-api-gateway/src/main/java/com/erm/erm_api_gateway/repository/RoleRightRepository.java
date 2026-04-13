package com.erm.erm_api_gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.erm_api_gateway.model.RoleRight;

@Repository
public interface RoleRightRepository extends JpaRepository<RoleRight, Long> {

	@Query("SELECT RR FROM RoleRight RR JOIN FETCH RR.role WHERE RR.id = :organization_id")
	public List<RoleRight> getRoleRightByOrganizationId(@Param("organization_id") Long organizationId);
}
