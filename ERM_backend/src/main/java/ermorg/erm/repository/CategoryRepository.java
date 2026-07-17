package ermorg.erm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query("""
			    SELECT DISTINCT c FROM Category c
			    JOIN ModuleOrganization mo ON mo.categoryId = c.id
			    LEFT JOIN FETCH c.fields f
			    WHERE mo.organization.id = :orgId
			      AND mo.moduleId = :moduleId
			      AND c.deleted = false
			    ORDER BY c.displayOrder
			""")
	List<Category> findAllByOrgAndModule(Long orgId, Long moduleId);

	@Query("""
			    SELECT DISTINCT c FROM Category c
			    LEFT JOIN FETCH c.fields f
			    WHERE c.module.id = :moduleId
			      AND c.mappedWithTable = :tableName
			      AND c.deleted = false
			    ORDER BY c.displayOrder
			""")
	List<Category> findAllByModuleIdAndMappedWithTableAndDeletedFalse(Long moduleId, String tableName);
}

