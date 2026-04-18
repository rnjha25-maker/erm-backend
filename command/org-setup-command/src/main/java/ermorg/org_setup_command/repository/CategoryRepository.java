package ermorg.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.org_setup_command.modal.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

