package ermorg.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department save(Department department);
}
