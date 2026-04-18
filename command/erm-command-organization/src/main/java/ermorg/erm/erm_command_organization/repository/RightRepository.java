package ermorg.erm.erm_command_organization.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.Right;

@Repository
public interface RightRepository extends JpaRepository<Right, Long> {
	
	 @Query("SELECT r FROM Right r WHERE r.id IN :rightIds")
	 List<Right> findAllByRightIds(@Param("rightIds") List<Long> rightIds);


}
