package ermorg.erm.erm_command_organization.repository;

import java.util.Optional;

import ermorg.erm.erm_command_organization.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department save(Department department);

    Optional<Department> findByIdAndDeletedFalse(Long id);
}
