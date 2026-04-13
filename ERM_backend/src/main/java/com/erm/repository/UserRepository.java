package com.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	@Query("SELECT u FROM User u WHERE u.company.id = :companyId")
	public List<User> getUsersByCompany(@Param("companyId") Long companyId);
	
	@Query("SELECT count(u) FROM User u WHERE u.organization.id = :orgId AND u.deleted = false")
	public long totalUsersByOrg(@Param("orgId")Long orgId);
	
	@Query("SELECT count(u) FROM User u WHERE u.organization.id = :orgId AND u.createdAt >= :startDate AND u.createdAt <= :endDate AND u.deleted = false")
	public long totalUsersByOrg(@Param("orgId")Long orgId, @Param("startDate") Date startDate,@Param("endDate") Date endDate);

	@Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.company.id = :companyId "
			+ "AND (u.deleted IS NULL OR u.deleted != true) AND LOWER(r.name) = LOWER(:roleName)")
	List<User> findByCompanyIdAndRoleNameIgnoreCase(@Param("companyId") Long companyId,
			@Param("roleName") String roleName);

}
