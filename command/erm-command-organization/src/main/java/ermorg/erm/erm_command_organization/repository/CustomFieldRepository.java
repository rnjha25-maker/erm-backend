package ermorg.erm.erm_command_organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.CustomField;

@Repository
public interface CustomFieldRepository extends JpaRepository<CustomField, Long> {

	@Modifying
	@Query("UPDATE CustomField f SET f.deleted = true WHERE f.category.id = :categoryId AND f.deleted = false")
	void softDeleteByCategoryId(@Param("categoryId") Long categoryId);

}
