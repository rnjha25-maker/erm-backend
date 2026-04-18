package ermorg.erm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.ModuleOrganization;

@Repository
public interface ModuleOrganizationRepository extends JpaRepository<ModuleOrganization, Long> {

	@Query("select m from ModuleOrganization m where m.organization.id = :id")
	public List<ModuleOrganization> getByOrganizationId(@Param("id") Long id);
}
