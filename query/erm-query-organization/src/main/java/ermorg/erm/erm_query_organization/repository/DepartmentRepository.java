package ermorg.erm.erm_query_organization.repository;

import ermorg.erm.erm_query_organization.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
