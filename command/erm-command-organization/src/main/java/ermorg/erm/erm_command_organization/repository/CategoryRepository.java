package ermorg.erm.erm_command_organization.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query("""
			    SELECT c FROM Category c
			    WHERE c.module.id = :moduleId
			      AND c.deleted = false
			    ORDER BY c.displayOrder
			""")
	List<Category> findOrderedCategories(@Param("moduleId") Long moduleId);
}

