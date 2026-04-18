package ermorg.erm.service;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

	@Query("SELECT COUNT(d) FROM Department d WHERE d.organization.id = :orgId AND d.deleted = false")
	public Long totalDepartments(@Param("orgId") Long orgId);
	
	@Query("SELECT COUNT(d) FROM Department d WHERE d.organization.id = :orgId AND d.createdAt BETWEEN :startDate AND :endDate AND d.deleted = false")
	public Long totalDepartments(@Param("orgId") Long orgId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
