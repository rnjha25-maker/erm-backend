package ermorg.erm.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.User;


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

	@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.roleType LEFT JOIN FETCH u.company "
			+ "LEFT JOIN FETCH u.branch LEFT JOIN FETCH u.department WHERE u.id = :id AND (u.deleted IS NULL OR u.deleted = false)")
	Optional<User> findActiveByIdWithRolesAndCompany(@Param("id") Long id);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.userDetail WHERE u.organization.id = :orgId "
			+ "AND (u.deleted IS NULL OR u.deleted = false)")
	List<User> findActiveUsersByOrganizationId(@Param("orgId") Long orgId);

}
