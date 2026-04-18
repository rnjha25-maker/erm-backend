package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.SystemField;

@Repository
public interface SystemFieldRepository extends JpaRepository<SystemField, Long> {

}
