package ermorg.erm.erm_command_organization.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.SystemTable;

@Repository
public interface SystemTableRepository extends JpaRepository<SystemTable, Long> {
	
	@Query("SELECT t FROM SystemTable t WHERE t.module.id = :moduleId AND t.deleted = false")
	public List<SystemTable> findAllByModuleId(@Param("moduleId") Long moduleId);

	@Query("SELECT t FROM SystemTable t WHERE t.tableName = :tableName AND t.deleted = false")
	public SystemTable findByTableName(String tableName);

}
